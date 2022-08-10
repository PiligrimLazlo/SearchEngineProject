package main.services;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import main.engine.Searcher;
import main.entities.SearchedPage;
import main.entities.Site;
import main.entities.Status;
import main.entities.responses.SearchResponse;
import main.repositories.*;
import main.utils.ApplicationProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private SiteRepository siteRepo;

    @Autowired
    private ApplicationProps applicationProps;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);


    @SneakyThrows
    @Override
    public SearchResponse search(String query, String sitePath, int offset, int limit) {
        List<SearchedPage> searchedPages = new ArrayList<>();
        if (query.isBlank())
            return new SearchResponse(false, "Задан пустой поисковый запрос");

        //если sitePath == null => задан поиск по всем сайтам
        if (sitePath != null) {
            Optional<Site> siteOpt = siteRepo.findByUrl(sitePath);
            if (siteOpt.isEmpty())
                return new SearchResponse(false, "Указанная страница не найдена");
            Site site = siteOpt.get();
            if (site.getStatus() == Status.INDEXING)
                return new SearchResponse(false, "Сайт \"" + site.getName() + "\" в данный момент индексируется");


            searchedPages.addAll(new Searcher(query, lemmaRepo, site.getId()).getSearchedPageList());
        } else {
            List<Site> allSites = Lists.newArrayList(siteRepo.findAll());
            List<Site> indexedSites =
                    allSites.stream().filter(site -> site.getStatus() == Status.INDEXED).collect(Collectors.toList());
            if (indexedSites.isEmpty())
                return new SearchResponse(false, "Все сайты индексируются");

            for (Site s : allSites) {
                searchedPages.addAll(new Searcher(query, lemmaRepo, s.getId()).getSearchedPageList());
            }
        }
        if (searchedPages.isEmpty())
            return new SearchResponse(false, "Ничего не найдено");
        return new SearchResponse(true, searchedPages.size(), searchedPages);

    }
}
