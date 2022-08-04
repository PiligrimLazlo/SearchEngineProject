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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
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
    private boolean isIndexing;

    private ExecutorService executorService;
    private DBCombiner dbCombiner;

    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() throws InterruptedException {
        System.out.println("sifewfwegfeg");
        List<Site> sites = applicationProps.getSites();

        if (isIndexing) return new IndexingResponse(false, "Индексация уже запущена");
        isIndexing = true;

        //List<Callable<IndexingResponse>> tasks = new ArrayList<>();
        for (Site s : sites) {
            executorService.submit(() -> {
                Site initializedSite = dbCombiner.initSiteBeforeIndexing(s, siteRepo);
                dbCombiner.createIndex(fieldRepo, indexRepo, siteRepo, initializedSite);
                //ошбки обрабатываются ниже в других частях кода
                return new IndexingResponse(true, null);
            });
        }

        //isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        List<Site> sites = applicationProps.getSites();

        if (!isIndexing) return new IndexingResponse(false, "Индексация не запущена");

        //todo stop indexing
        //executorService.shutdownNow();
        //dbCombiner.stopIndexing();

        isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @PostMapping("/indexPage")
    public IndexingResponse indexPage(@RequestParam String url) throws InterruptedException, ExecutionException {
        List<Site> sitesFromYml = applicationProps.getSites();
        List<Site> siteWithSpecifiedUrl =
                sitesFromYml.stream().filter(site -> site.getUrl().equals(url)).collect(Collectors.toList());
        if (siteWithSpecifiedUrl.isEmpty())
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");


        executorService.submit(() -> {
            Site initializedSite = dbCombiner.initSiteBeforeIndexing(siteWithSpecifiedUrl.get(0), siteRepo);
            dbCombiner.createIndex(fieldRepo, indexRepo, siteRepo, initializedSite);
        });

        //ошбки обрабатываются ниже в других частях кода
        return new IndexingResponse(true, null);
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        //initialized here because this method run first
        executorService = Executors.newFixedThreadPool(applicationProps.getSites().size());
        dbCombiner = new DBCombiner();

        long sitesCount = siteRepo.count();

        TotalStatistics totalStatistics =
                new TotalStatistics(sitesCount, pageRepo.count(), lemmaRepo.count(), isIndexing);

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
