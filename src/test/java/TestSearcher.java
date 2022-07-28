import main.engine.Lemmatizer;
import main.engine.Searcher;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestSearcher {

    private Searcher searcher;
    private String text;
    private Set<Index> testIndexes;


    @Before
    public void setUp() throws IOException {
        text = "свой жизнь";
        testIndexes = createTestData();
        searcher = new Searcher(text, new ArrayList<>(testIndexes));
    }

    @Test
    public void testGetIntersectionOfSearchAndSiteLemmas() {
        //TODO
    }

    @Test
    public void testGetPagesContainingIntersectedLemmas() {
        //TODO
    }

    @Test
    public void testCreateSearchedPageList() {
        //TODO
    }



    private Set<Index> createTestData() {
        Page page1 = new Page();
        page1.setId(1);
        page1.setPath("https://google.com/");
        page1.setCode(200);
        page1.setContent("<title>Google site!</title> <body>Welcome to the internet</body>");

        Page page2 = new Page();
        page2.setId(2);
        page2.setPath("https://yandex.ru/");
        page2.setCode(200);
        page2.setContent("<title>Yandex site!</title> <body>Welcome to the ru internet</body>");

        Page page3 = new Page();
        page3.setId(3);
        page3.setPath("https://wikipedia.org/");
        page3.setCode(200);
        page3.setContent("<title>Wikipedia!</title> <body>World biggest encyclopedia</body>");

        Lemma lemma1 = new Lemma();
        lemma1.setId(1);
        lemma1.setLemma("компьютер");
        lemma1.setFrequency(10);

        Lemma lemma2 = new Lemma();
        lemma2.setId(2);
        lemma2.setLemma("стол");
        lemma2.setFrequency(20);

        Lemma lemma3 = new Lemma();
        lemma3.setId(3);
        lemma3.setLemma("ручка");
        lemma3.setFrequency(30);

        Set<Index> testIndexes = new HashSet<>();
        Index index1 = new Index();
        index1.setId(1);
        index1.setPage(page1);
        index1.setLemma(lemma1);
        index1.setRank(5);

        Index index2 = new Index();
        index2.setId(2);
        index2.setPage(page2);
        index2.setLemma(lemma2);
        index2.setRank(7);

        Index index3 = new Index();
        index3.setId(3);
        index3.setPage(page3);
        index3.setLemma(lemma3);
        index3.setRank(9);

        page1.setIndexes(testIndexes);
        page2.setIndexes(testIndexes);
        page3.setIndexes(testIndexes);

        lemma1.setIndexes(testIndexes);
        lemma2.setIndexes(testIndexes);
        lemma3.setIndexes(testIndexes);

        return testIndexes;
    }

}

