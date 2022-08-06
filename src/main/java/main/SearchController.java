package main;

import com.google.common.collect.Lists;
import main.engine.DBCombiner;
import main.model.*;
import main.model.responses.IndexingResponse;
import main.model.responses.statistics.DetailedStatistics;
import main.model.responses.statistics.Statistics;
import main.model.responses.statistics.StatisticsResponse;
import main.model.responses.statistics.TotalStatistics;
import main.utils.ApplicationProps;
import main.utils.DBCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
public class SearchController {

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
    private DBCombiner dbCombiner;

    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() throws InterruptedException, SQLException {
        List<Site> sites = applicationProps.getSites();

        if (isIndexingAll) return new IndexingResponse(false, "Индексация уже запущена");
        isIndexingAll = true;

        //dbCombiner.cancelParsing();
        //DBCreator.initDb();

        for (Site s : sites) {
            if (s.getStatus() == Status.INDEXING) continue;
            startIndexingSite(s);
        }

        //isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        if (!isIndexingAll) return new IndexingResponse(false, "Индексация не запущена");
        dbCombiner.cancelParsing();
        isIndexingAll = false;
        return new IndexingResponse(true, null);
    }

    @PostMapping("/indexPage")
    public IndexingResponse indexPage(@RequestParam String url) throws InterruptedException, ExecutionException, SQLException {
        List<Site> sitesFromYml = applicationProps.getSites();
        List<Site> siteWithSpecifiedUrl =
                sitesFromYml.stream().filter(site -> site.getUrl().equals(url)).collect(Collectors.toList());
        if (siteWithSpecifiedUrl.isEmpty())
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");

        Site currentSite = siteWithSpecifiedUrl.get(0);
        if (currentSite.getStatus() == Status.INDEXING) {
            return new IndexingResponse(false, "Указанный сайт уже индексируется");
        }

        startIndexingSite(currentSite);

        //ошбки обрабатываются ниже в других частях кода
        return new IndexingResponse(true, null);
    }

    private void startIndexingSite(Site currentSite) throws SQLException {
        DBCreator.removeFromPageTable(currentSite.getId());
        DBCreator.removeFromLemmaTable(currentSite.getId());

        executorService.submit(() -> {
            Site initializedSite = dbCombiner.initSiteBeforeIndexing(currentSite, siteRepo);
            dbCombiner.createIndex(fieldRepo, indexRepo, siteRepo, initializedSite);
        });
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        //initialized here because this method run first
        executorService = Executors.newFixedThreadPool(applicationProps.getSites().size());
        dbCombiner = new DBCombiner();

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
}
