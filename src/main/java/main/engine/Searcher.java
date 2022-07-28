package main.engine;

import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.SearchedPage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Searcher {

    private List<SearchedPage> searchedPageList;

    public Searcher(String textToSearch, List<Index> indexes) throws IOException {

        List<Lemma> intersectedLemmas = getIntersectionOfSearchAndSiteLemmas(textToSearch, indexes);

        List<Page> foundPages = getPagesContainingIntersectedLemmas(intersectedLemmas);
        if (foundPages.isEmpty()) return;

        searchedPageList = createSearchedPageList(foundPages);
    }

    public List<SearchedPage> getSearchedPageList() {
        return searchedPageList;
    }


    /**
     * Разбиваем поисковый запрос на отдельные слова.
     * Формируем из этих слов список уникальных лемм. Исключаем из списка слов междометия, союзы, предлоги и частицы.
     * Исключаем леммы, которые встречаются большом количестве страниц (> 100).
     * Сортируем леммы в порядке увеличения частоты встречаемости (по возрастанию значения поля frequency)
     * — от самых редких до самых частых.
     */
    public List<Lemma> getIntersectionOfSearchAndSiteLemmas(String textToSearch, List<Index> indexes) throws IOException {
        Set<String> searchLemmas = Lemmatizer.getInstance().getLemmaSet(textToSearch);

        return indexes.stream().map(index -> index.getLemma())
                .distinct()
                .filter(siteLemma -> searchLemmas.contains(siteLemma.getLemma()) && siteLemma.getFrequency() < 100)
                .sorted((l1, l2) -> Integer.compare(l1.getFrequency(), l2.getFrequency()))
                .collect(Collectors.toList());
    }


    /**
     * По первой, самой редкой лемме из списка, находим все страницы, на которых она встречается.
     * Далее ищем соответствия следующей леммы и этого списка страниц, и так по каждой следующей лемме.
     * Список страниц при этом на каждой итерации уменьшается.
     * Если в итоге не осталось ни одной страницы, выводим пустой список.
     */
    public List<Page> getPagesContainingIntersectedLemmas(List<Lemma> intersectedLemmas) {
        List<Page> foundPages = new ArrayList<>();


        if (!intersectedLemmas.isEmpty()) {
            foundPages = intersectedLemmas.get(0)
                    .getIndexes()
                    .stream()
                    .map(index -> index.getPage())
                    .collect(Collectors.toList());
        }


        if (intersectedLemmas.size() > 1) {
            int count = 1;
            while (count != intersectedLemmas.size()) {

                Lemma anotherLemma = intersectedLemmas.get(count);

                List<Page> finalSearchedPages = foundPages;
                foundPages = anotherLemma
                        .getIndexes()
                        .stream()
                        .map(index -> index.getPage())
                        .filter(anotherPage -> finalSearchedPages.contains(anotherPage))
                        .collect(Collectors.toList());

                if (foundPages.isEmpty()) break;

                count++;
            }
        }

        return foundPages;
    }


    /**
     *Если страницы (Page  в методе getPagesContainingIntersectedLemmas(List<Lemma> intersectedLemmas)) найдены,
     * рассчитывать по каждой из них релевантность.
     * Для этого для каждой страницы рассчитываем абсолютную релевантность —
     * сумму всех rank всех найденных на странице лемм (из таблицы index),
     * которую делим на максимальное значение этой абсолютной релевантности для всех найденных страниц.
     */
    public List<SearchedPage> createSearchedPageList(List<Page> foundPages) {
        List<SearchedPage> searchedPageList = new ArrayList<>();

        double[] pagesRelevance = new double[foundPages.size()];
        double maxAbsRelevance = 0;
        for (int i = 0; i < pagesRelevance.length; i++) {
            Double absRelevance = foundPages.get(i)
                    .getIndexes()
                    .stream()
                    .reduce(0.0, (r1, i2) -> r1 + i2.getRank(), (r1, r2) -> r1 + r2);
            pagesRelevance[i] = absRelevance;
        }
        maxAbsRelevance = Arrays.stream(pagesRelevance).max().getAsDouble();

        for (int i = 0; i < pagesRelevance.length; i++) {
            Page p = foundPages.get(i);
            SearchedPage searchedPage = new SearchedPage();
            searchedPage.setUri(p.getPath());
            searchedPage.setTitle(p.getContent().substring(p.getContent().indexOf("<title>") + 7, p.getContent().indexOf("</title>")));
            searchedPage.setSnippet("<b>" + "the snippet" + "</b>"); //TODO где взять snippet
            searchedPage.setRelevance(pagesRelevance[i] / maxAbsRelevance);
            searchedPageList.add(searchedPage);
        }

        searchedPageList.sort(Comparator.comparingDouble(SearchedPage::getRelevance));

        return searchedPageList;
    }
}
