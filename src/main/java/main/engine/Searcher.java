package main.engine;

import main.model.Index;
import main.model.Lemma;
import main.model.LemmaRepository;
import main.model.Page;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Searcher {
    private final String text;
    private final Set<String> searchLemmas;

    private final List<Index> siteIndexes;
    private final List<Lemma> siteLemmas;
    private final List<Page> sitePages;

    public Searcher(String text, List<Index> indexes) throws IOException {
        this.text = text;
        searchLemmas = Lemmatizer.getInstance().getLemmaSet(text);
        this.siteIndexes = indexes;
        siteLemmas = new ArrayList<>();
        sitePages = new ArrayList<>();


/*        indexes.stream()
                .filter(index -> index.getLemma().getFrequency() < 100)
                .sorted((i1, i2) -> Integer.compare(i1.getLemma().getFrequency(), i2.getLemma().getFrequency()));


        //indexes.forEach(index -> siteLemmas.add(index.getLemma()));

        siteLemmas.stream()
                .filter(lemma -> lemma.getFrequency() < 100)
                .sorted((l1, l2) -> Integer.compare(l1.getFrequency(), l2.getFrequency())).collect(Collectors.toList());*/
    }


}
