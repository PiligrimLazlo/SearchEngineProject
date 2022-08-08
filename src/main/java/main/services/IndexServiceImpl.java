package main.services;

import com.google.common.collect.Lists;
import main.engine.IndexDBCombiner;
import main.entities.Site;
import main.entities.Status;
import main.entities.responses.IndexResponse;
import main.entities.responses.statistics.DetailedStatistics;
import main.entities.responses.statistics.Statistics;
import main.entities.responses.statistics.StatisticsResponse;
import main.entities.responses.statistics.TotalStatistics;
import main.repositories.*;
import main.utils.ApplicationProps;
import main.utils.DBCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private FieldRepository fieldRepo;
    @Autowired
    private LemmaRepository lemmaRepo;
    @Autowired
    private PageRepository pageRepo;
    @Autowired
    private IndexRepository indexRepo;
    @Autowired
    private SiteRepository siteRepo;

    @Autowired
    private ApplicationProps applicationProps;

    private boolean isIndexingAll;
    private ExecutorService executorService;
    private IndexDBCombiner indexDbCombiner;


    @Override
    public IndexResponse startIndexing() {
        List<Site> sites = applicationProps.getSites();

        if (isIndexingAll) return new IndexResponse(false, "Индексация уже запущена");
        isIndexingAll = true;

        for (Site s : sites) {
            if (s.getStatus() == Status.INDEXING) continue;
            startIndexingSite(s);
        }

        return new IndexResponse(true);
    }

    @Override
    public IndexResponse stopIndexing() {
        if (!isIndexingAll) return new IndexResponse(false, "Индексация не запущена");
        indexDbCombiner.cancelParsing();
        isIndexingAll = false;
        return new IndexResponse(true);
    }

    @Override
    public IndexResponse indexPage(String url) {
        if (url.isBlank())
            return new IndexResponse(false, "Задан пустой запрос");
        List<Site> sitesFromYml = applicationProps.getSites();
        List<Site> siteWithSpecifiedUrl =
                sitesFromYml.stream().filter(site -> site.getUrl().equals(url)).collect(Collectors.toList());
        if (siteWithSpecifiedUrl.isEmpty())
            return new IndexResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");

        Site currentSite = siteWithSpecifiedUrl.get(0);
        if (currentSite.getStatus() == Status.INDEXING) {
            return new IndexResponse(false, "Указанный сайт уже индексируется");
        }

        startIndexingSite(currentSite);

        //ошбки обрабатываются ниже в других частях кода
        return new IndexResponse(true);
    }

    @Override
    public StatisticsResponse getStatistics() {
        //initialized here because this method run first
        executorService = Executors.newFixedThreadPool(applicationProps.getSites().size());
        indexDbCombiner = new IndexDBCombiner();

        long sitesCount = siteRepo.count();

        TotalStatistics totalStatistics =
                new TotalStatistics(sitesCount, pageRepo.count(), lemmaRepo.count(), isIndexingAll);

        List<DetailedStatistics> detailedStatistics =
                Lists.newArrayList(siteRepo.findAll()).stream().map(site -> new DetailedStatistics(
                        site.getUrl(),
                        site.getName(),
                        site.getStatus(),
                        site.getStatusTime().getTime(),
                        site.getLastError(),
                        pageRepo.countPages(site.getId()),
                        lemmaRepo.countLemmas(site.getId())
                )).collect(Collectors.toList());

        Statistics statistics = new Statistics(totalStatistics, detailedStatistics);

        return new StatisticsResponse(true, statistics);
    }

    private void startIndexingSite(Site currentSite) {
        try {
            DBCreator.removeFromPageTable(currentSite.getId());
            DBCreator.removeFromLemmaTable(currentSite.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        executorService.submit(() -> {
            Site initializedSite = indexDbCombiner.initSiteBeforeIndexing(currentSite, siteRepo);
            indexDbCombiner.createIndex(fieldRepo, indexRepo, siteRepo, initializedSite);
        });
    }
}
