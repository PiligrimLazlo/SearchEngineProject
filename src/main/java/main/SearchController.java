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

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        System.out.println("sifewfwegfeg");
        List<Site> sites = applicationProps.getSites();

        if (isIndexing) return new IndexingResponse(false, "Индексация уже запущена");
        isIndexing = true;

        ExecutorService executorService = Executors.newFixedThreadPool(sites.size());

        /*for (Site s: sites) {
            //todo
            executorService.submit(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }*/

        isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        List<Site> sites = applicationProps.getSites();

        if (!isIndexing) return new IndexingResponse(false, "Индексация не запущена");

        //todo stop indexing

        isIndexing = false;
        return new IndexingResponse(true, null);
    }

    @PostMapping("/indexPage")
    public IndexingResponse indexPage(@RequestParam String url) {
        List<Site> sitesFromYml = applicationProps.getSites();
        List<Site> siteWithSpecifiedUrl =
                sitesFromYml.stream().filter(site -> site.getUrl().equals(url)).collect(Collectors.toList());
        if (siteWithSpecifiedUrl.isEmpty())
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");


        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        singleThreadExecutor.submit(() -> {
            Site initializedSite = DBCombiner.initSiteBeforeIndexing(siteWithSpecifiedUrl.get(0), siteRepo);
            DBCombiner.createIndex(fieldRepo, indexRepo, siteRepo, initializedSite);
        });

        return new IndexingResponse(true, null);
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        long sitesCount = siteRepo.count();
        long pagesCount = pageRepo.count();
        long lemmasCount = lemmaRepo.count();

        TotalStatistics totalStatistics =
                new TotalStatistics(sitesCount, pagesCount, lemmasCount, isIndexing);

        List<DetailedStatistics> detailedStatistics = DetailedStatistics.getDetailedStatistics(
                Lists.newArrayList(siteRepo.findAll()), pagesCount, lemmasCount);

        Statistics statistics = new Statistics(totalStatistics, detailedStatistics);

        return new StatisticsResponse(true, statistics);
    }


}
