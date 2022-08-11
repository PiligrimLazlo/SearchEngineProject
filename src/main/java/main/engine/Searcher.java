package main.engine;

import lombok.SneakyThrows;
import main.entities.Lemma;
import main.entities.Page;
import main.entities.SearchedPage;
import main.repositories.LemmaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Searcher {

    private List<SearchedPage> searchedPageList;
    private String textToSearch;

    public Searcher(String textToSearch, LemmaRepository lemmaRepo, int siteId) throws IOException {
        this.textToSearch = textToSearch;
        List<Lemma> intersectedLemmas = getIntersectionOfSearchAndSiteLemmas(textToSearch, lemmaRepo, siteId);

        List<Page> foundPages = getPagesContainingIntersectedLemmas(intersectedLemmas);
        if (foundPages.isEmpty())
            searchedPageList = new ArrayList<>();
        else
            searchedPageList = createSearchedPageList(foundPages);
    }

    public List<SearchedPage> getSearchedPageList() {
        return searchedPageList;
    }


    /**
     * Разбиваем поисковый запрос на отдельные слова.
     * Формируем из этих слов список уникальных лемм. Исключаем из списка слов междометия, союзы, предлоги и частицы.
     * Исключаем леммы, которые встречаются большом количестве страниц (> 80).
     * Сортируем леммы в порядке увеличения частоты встречаемости (по возрастанию значения поля frequency)
     * — от самых редких до самых частых.
     */
    private List<Lemma> getIntersectionOfSearchAndSiteLemmas(
            String textToSearch, LemmaRepository lemmaRepo, int siteId) throws IOException {

        Set<String> searchLemmas = Lemmatizer.getInstance().getLemmaSet(textToSearch);
        return lemmaRepo.selectIntersectionOfSearchAndSiteLemmasFromDb(siteId, searchLemmas.toArray(new String[0]));
    }


    /**
     * По первой, самой редкой лемме из списка, находим все страницы, на которых она встречается.
     * Далее ищем соответствия следующей леммы и этого списка страниц, и так по каждой следующей лемме.
     * Список страниц при этом на каждой итерации уменьшается.
     * Если в итоге не осталось ни одной страницы, выводим пустой список.
     */
    private List<Page> getPagesContainingIntersectedLemmas(List<Lemma> intersectedLemmas) {
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
     * Если страницы (Page  в методе getPagesContainingIntersectedLemmas(List<Lemma> intersectedLemmas)) найдены,
     * рассчитывать по каждой из них релевантность.
     * Для этого для каждой страницы рассчитываем абсолютную релевантность —
     * сумму всех rank всех найденных на странице лемм (из таблицы index),
     * которую делим на максимальное значение этой абсолютной релевантности для всех найденных страниц.
     */
    private List<SearchedPage> createSearchedPageList(List<Page> foundPages) {
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
            searchedPage.setUri(p.getPath());//p.getPath().substring(0, p.getPath().lastIndexOf("/"))

            int start = p.getContent().indexOf("<title>") + 7;
            int end = p.getContent().indexOf("</title>");
            if (start == -1 + 7 || end == -1)
                searchedPage.setTitle("-");
            else
                searchedPage.setTitle(p.getContent().substring(start, end));

            searchedPage.setSnippet(getSnippetsForCurrentPage(p, textToSearch));
            searchedPage.setRelevance(pagesRelevance[i] / maxAbsRelevance);
            searchedPage.setSite(p.getSite().getUrl().substring(0, p.getSite().getUrl().lastIndexOf("/")));
            searchedPage.setSiteName(p.getSite().getName());
            searchedPageList.add(searchedPage);
        }

        searchedPageList.sort(Comparator.comparingDouble(SearchedPage::getRelevance));

        return searchedPageList;
    }

    /**
     * Метод для получения фрагмента текста, в котором найдены совпадения, выделенные жирным, в формате HTML (этап 5 пункт 7)
     * В качестве фрагментов приняты корневые элементы страницы (без дочерних, сущность Element из Jsoup)
     * Для каждого такого Element мы извлекаем все леммы и далее находим пересечения с леммами из поискового запроса.
     * При совпадении обрамляем в исходной строке (полученной путем вызова Element.toString()) слово тегами b
     * Возвращаем строку равную исходному Element с леммами, выделенными жирным
     */
    private String getSnippetsForCurrentPage(Page currentPage, String text) {
        Lemmatizer lemmatizer = null;
        try {
            lemmatizer = Lemmatizer.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<String> lemmaSet = lemmatizer.getLemmaSet(text);
        Document doc = Jsoup.parse(currentPage.getContent());

        List<Element> elements = new ArrayList<>();
        doc.forEach(element -> {
            if (element.childrenSize() == 0) {
                elements.add(element);
            } else if (element.childrenSize() > 0
                    && element.toString().length() > 500
                    && element.toString().length() < 2000) {
                elements.add(element);
            }
        });

        StringBuilder snippets = new StringBuilder();
        for (Element element : elements) {
            String startElementString = Jsoup.clean(element.toString(), Safelist.basic());
            String result = startElementString;
            String cleanElement = Jsoup.clean(element.toString(), Safelist.none());

            if (cleanElement.isBlank()) continue;

            String[] words = russianWordArrayFromString(cleanElement);

            for (String w : words) {
                Optional<String> curLemmaOpt = lemmatizer.getLemmaSet(w).stream().findFirst();
                String curLemma;

                if (curLemmaOpt.isPresent()) {
                    curLemma = curLemmaOpt.get();
                } else {
                    continue;
                }

                if (lemmaSet.contains(curLemma)) {
                    result = result.replaceAll("[^А-Яа-я]" + w + "[^А-Яа-я]", "<b> " + w + " </b>");
                }
            }
            if (!startElementString.equals(result)) {
                snippets.append(result);
            }
        }
        return snippets.toString();
    }

    private String[] russianWordArrayFromString(String text) {
        return text.replaceAll("([^а-яА-Я\\s])", " ")
                .trim()
                .split("\\s+");
    }
}
