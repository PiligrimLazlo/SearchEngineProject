package main.engine;

import main.model.*;
import main.utils.DBCreator;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class DBCombiner {

    /**
     * перезаписывает БД и индексирует переданный сайт
     */
    public List<Index> createIndex(
            FieldRepository fieldRepo,
            IndexRepository indexRepo,
            SiteRepository siteRepo,
            Site site
    ) {
        List<Index> indexes = null;
        try {

            Parser parser = new Parser(site.getUrl());
            parser.setFieldForIndex(fieldRepo.findAll());

            ForkJoinPool pool = ForkJoinPool.commonPool();
            pool.invoke(parser);

            indexes = parser.getIndexes();

            //TODO перенести в Parser (или нет)
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXED);
            indexes.stream().map(Index::getPage).forEach(page -> page.setSite(site));
            indexes.stream().map(Index::getLemma).forEach(lemma -> lemma.setSite(site));

            indexRepo.saveAll(indexes);
        } catch (Exception e) {
            site.setStatus(Status.FAILED);
            site.setLastError(e.getMessage());
            e.printStackTrace();
        }
        siteRepo.save(site);
        return indexes;
    }

/*    public static Site createCurrentSite(SiteRepository siteRepo, String sitePath, String siteName) {
        Optional<Site> siteOpt = siteRepo.findByUrl(sitePath);
        Site currentSite = siteOpt.orElseGet(Site::new);
        currentSite.setName(siteName);
        currentSite.setStatus(Status.INDEXING);
        currentSite.setUrl(sitePath);
        currentSite.setStatusTime(new Date());
        siteRepo.save(currentSite);

        return currentSite;
    }*/

    public Site initSiteBeforeIndexing(Site ymlSite, SiteRepository siteRepo) {
        ymlSite.setStatus(Status.INDEXING);
        ymlSite.setStatusTime(new Date());
        siteRepo.save(ymlSite);

        return ymlSite;
    }

}
