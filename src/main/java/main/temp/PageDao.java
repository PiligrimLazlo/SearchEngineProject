package main.temp;

import main.model.Page;

import java.util.List;

public interface PageDao {

    //get
    Page getPageById(int id);

    Page getPageByPath(String path);

    List<Page> getAllPages();

    //create
    void createPage(Page page);

    void createAll(List<Page> pages);

    //delete
    boolean deletePage(int id);

    void deleteAllPages();

    //update
    boolean updatePage(int id, Page updatedPage);



}
