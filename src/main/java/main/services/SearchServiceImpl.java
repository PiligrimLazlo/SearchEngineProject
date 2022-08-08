package main.services;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import main.engine.Searcher;
import main.entities.Index;
import main.entities.SearchedPage;
import main.entities.Site;
import main.entities.Status;
import main.entities.responses.FoundPage;
import main.entities.responses.SearchResponse;
import main.repositories.*;
import main.utils.ApplicationProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

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

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);


    @SneakyThrows
    @Override
    public SearchResponse search(String query, String sitePath, int offset, int limit) {
        //too slow
        List<Index> indexes = Lists.newArrayList(indexRepo.findAll());
        List<FoundPage> searchedPages = new ArrayList<>();


        //если sitePath == null => задан поиск по всем сайтам
        if (sitePath != null) {
            if (query.isBlank())
                return new SearchResponse(false, "Задан пустой поисковый запрос");
            Optional<Site> siteOpt = siteRepo.findByUrl(sitePath);
            if (siteOpt.isEmpty())
                return new SearchResponse(false, "Указанная страница не найдена");
            Site site = siteOpt.get();
            if (site.getStatus() == Status.INDEXING)
                return new SearchResponse(false, "Сайт \"" + site.getName() + "\" в данный момент индексируется");

            searchedPages.addAll(getSearchResultForCurrentPage(indexes, List.of(sitePath), query));
        } else {
            List<Site> allSites = Lists.newArrayList(siteRepo.findAll());
            List<Site> indexedSites =
                    allSites.stream().filter(site -> site.getStatus() == Status.INDEXED).collect(Collectors.toList());
            if (indexedSites.isEmpty())
                return new SearchResponse(false, "Все сайты индексируются");


            List<String> allSitePaths = allSites.stream()
                    .map(Site::getUrl)
                    .collect(Collectors.toList());
            searchedPages.addAll(getSearchResultForCurrentPage(indexes, allSitePaths, query));
        }
        if (searchedPages.isEmpty())
            return new SearchResponse(false, "Ничего не найдено");
        return new SearchResponse(true, searchedPages.size(), searchedPages);

    }

    private List<FoundPage> getSearchResultForCurrentPage(List<Index> allIndexes, List<String> sitePaths, String query) {
        List<Callable<List<FoundPage>>> pages = new ArrayList<>();

        for (String currentSitePath : sitePaths) {
            List<Index> currentSiteIndexes = allIndexes.stream()
                    .filter(index -> index.getPage().getSite().getUrl().equals(currentSitePath))
                    .collect(Collectors.toList());

            pages.add(() -> {
                try {
                    Searcher searcher = new Searcher(query, currentSiteIndexes);
                    String siteName = applicationProps
                            .getSites()
                            .stream()
                            .filter(site -> site.getUrl().equals(currentSitePath))
                            .map(Site::getName)
                            .findFirst().orElseThrow();

                    return searcher.getSearchedPageList().stream().map(searchedPage -> new FoundPage(
                            currentSitePath.substring(0, currentSitePath.lastIndexOf("/")),
                            siteName,
                            searchedPage.getUri(),
                            searchedPage.getTitle(),
                            searchedPage.getSnippet(),
                            searchedPage.getRelevance()
                    )).collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            });
        }

        List<FoundPage> result = new ArrayList<>();
        try {
            List<Future<List<FoundPage>>> futures = executorService.invokeAll(pages);
            for (Future<List<FoundPage>> future : futures) {
                result.addAll(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
