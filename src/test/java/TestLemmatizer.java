import main.engine.Lemmatizer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestLemmatizer {

    Lemmatizer lemmatizer;

    @Before
    public void setUp() throws IOException {
        lemmatizer = Lemmatizer.getInstance();
    }

    @Test
    public void testCollectLemmas() {
        String text = "Брат брата не выдаст";
        Map<String, Integer> actualMap = lemmatizer.collectLemmas(text);
        Map<String, Integer> expectedMap = Map.of(
                "выдать", 1,
                "не", 1,
                "брат", 2
        );
        assertEquals(actualMap, expectedMap);
    }

    @Test
    public void testGetLemmaSet() {
        String text = "Брат брата не выдаст";
        Set<String> actualSet = lemmatizer.getLemmaSet(text);
        Set<String> expectedSet = Set.of("выдать", "брат", "не");
        assertEquals(actualSet, expectedSet);
    }
}
