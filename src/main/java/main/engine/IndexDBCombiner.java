package main.engine;

import main.entities.Index;
import main.entities.Site;
import main.entities.Status;
import main.repositories.FieldRepository;
import main.repositories.IndexRepository;
import main.repositories.SiteRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class IndexDBCombiner {

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
            Parser.setCanceled(false);
            Parser parser = new Parser(site.getUrl());
            parser.setFieldForIndex(fieldRepo.findAll());

            ForkJoinPool pool = ForkJoinPool.commonPool();
            pool.invoke(parser);

            indexes = parser.getIndexes();

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

    /**
     * инициализирует сайт в БД перед создание индекса. Нужно вызвать перед createIndex(~)
     */
    public Site initSiteBeforeIndexing(Site ymlSite, SiteRepository siteRepo) {
        ymlSite.setStatus(Status.INDEXING);
        ymlSite.setStatusTime(new Date());
        siteRepo.save(ymlSite);

        return ymlSite;
    }

    public void cancelParsing() {
        Parser.setCanceled(true);
    }

    public int getPagesCount(String site) {
        return Parser.getPagesCount(site);
    }

    public int getLemmasCount(String site) {
        return Parser.getLemmaCount(site);
    }
}
