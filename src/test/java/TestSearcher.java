import main.engine.Lemmatizer;
import main.engine.Searcher;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSearcher {

    private String text;
    ArrayList<Index> indexList;
    private Searcher searcher;

    @Before
    public void setUp() throws IOException {
        //test data такая <=> 2 и 3 страницы содержат леммы 2 и 3
        text = "ручек столов";
        indexList = new ArrayList<>(createTestIndexes());
        searcher = new Searcher(text, indexList);
    }



    @Test
    public void testGetIntersectionOfSearchAndSiteLemmas() throws IOException {
        List<Lemma> intersectionOfSearchAndSiteLemmas =
                searcher.getIntersectionOfSearchAndSiteLemmas(text, indexList);

        List<Lemma> expectedLemmas = createTestLemmas();
        expectedLemmas.remove(0);

        assertEquals(expectedLemmas, intersectionOfSearchAndSiteLemmas);
    }

    @Test
    public void testGetPagesContainingIntersectedLemmas() throws IOException {

        List<Lemma> intersectedLemmas = searcher.getIntersectionOfSearchAndSiteLemmas(text, indexList);
        List<Page> pagesContainingIntersectedLemmas = searcher.getPagesContainingIntersectedLemmas(intersectedLemmas);

        List<Page> expectedPages = createTestPages();
        expectedPages.remove(0);

        expectedPages.forEach(page -> assertTrue(pagesContainingIntersectedLemmas.contains(page)));
    }


    private List<Lemma> createTestLemmas() {
        List<Lemma> result = new ArrayList<>();

        Lemma lemma1 = new Lemma();
        lemma1.setId(1);
        lemma1.setLemma("компьютер");
        lemma1.setFrequency(10);
        result.add(lemma1);

        Lemma lemma2 = new Lemma();
        lemma2.setId(2);
        lemma2.setLemma("стол");
        lemma2.setFrequency(20);
        result.add(lemma2);

        Lemma lemma3 = new Lemma();
        lemma3.setId(3);
        lemma3.setLemma("ручка");
        lemma3.setFrequency(30);
        result.add(lemma3);

        return result;
    }

    private List<Page> createTestPages() {
        List<Page> result = new ArrayList<>();

        Page page1 = new Page();
        page1.setId(1);
        page1.setPath("https://google.com/");
        page1.setCode(200);
        page1.setContent("<title>Google site!</title> <body>Welcome to the internet</body>");
        result.add(page1);

        Page page2 = new Page();
        page2.setId(2);
        page2.setPath("https://yandex.ru/");
        page2.setCode(200);
        page2.setContent("<title>Yandex site!</title> <body>Welcome to the ru internet</body>");
        result.add(page2);

        Page page3 = new Page();
        page3.setId(3);
        page3.setPath("https://wikipedia.org/");
        page3.setCode(200);
        page3.setContent("<title>Wikipedia!</title> <body>World biggest encyclopedia</body>");
        result.add(page3);

        return result;
    }



    private Set<Index> createTestIndexes() {
        List<Page> testPages = createTestPages();
        List<Lemma> testLemmas = createTestLemmas();


        Set<Index> testIndexes = new HashSet<>();
        Index index1 = new Index();
        index1.setId(1);
        index1.setPage(testPages.get(0));
        index1.setLemma(testLemmas.get(0));
        index1.setRank(5);
        testIndexes.add(index1);


        Index index2 = new Index();
        index2.setId(2);
        index2.setPage(testPages.get(1));
        index2.setLemma(testLemmas.get(1));
        index2.setRank(7);
        testIndexes.add(index2);


        Index index3 = new Index();
        index3.setId(3);
        index3.setPage(testPages.get(2));
        index3.setLemma(testLemmas.get(2));
        index3.setRank(9);
        testIndexes.add(index3);



        testPages.get(0).setIndexes(Set.of(index1));
        testLemmas.get(0).setIndexes(Set.of(index1));
        testPages.get(1).setIndexes(Set.of(index2, index3));
        testLemmas.get(1).setIndexes(Set.of(index2, index3));
        testPages.get(2).setIndexes(Set.of(index2, index3));
        testLemmas.get(2).setIndexes(Set.of(index2, index3));

        return testIndexes;
    }

}

