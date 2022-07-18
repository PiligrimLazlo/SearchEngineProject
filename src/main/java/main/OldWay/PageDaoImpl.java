package main.OldWay;

import main.model.Page;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class PageDaoImpl implements PageDao {

    private Session currentSession;
    private Transaction currentTransaction;

    private Session openCurrentSessionWithTransaction() {
        currentSession = HibernateSessionFactory.getSessionFactory().openSession();
        currentTransaction = currentSession.beginTransaction();
        return currentSession;
    }

    private void closeCurrentSessionWithTransaction() {
        currentTransaction.commit();
        currentSession.close();
    }


    @Override
    public Page getPageById(int id) {
        openCurrentSessionWithTransaction();

        Page page = currentSession.get(Page.class, id);

        closeCurrentSessionWithTransaction();
        return page;
    }

    @Override
    public Page getPageByPath(String path) {
        openCurrentSessionWithTransaction();

        Query query = currentSession.createQuery("from Page where path = :path");
        query.setParameter("path", path);
        Page page = (Page) query.uniqueResult();

        closeCurrentSessionWithTransaction();
        return page;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Page> getAllPages() {
        openCurrentSessionWithTransaction();


        List<Page> pages = (List<Page>) currentSession.createQuery("from Page").list();

        closeCurrentSessionWithTransaction();
        return pages;
    }

    @Override
    public void createPage(Page page) {
        openCurrentSessionWithTransaction();

        currentSession.save(page);

        closeCurrentSessionWithTransaction();
    }

    @Override
    public void createAll(List<Page> pages) {
        openCurrentSessionWithTransaction();

        for (Page page : pages) {
            currentSession.save(page);
        }

        closeCurrentSessionWithTransaction();
    }

    @Override
    public boolean deletePage(int id) {
        openCurrentSessionWithTransaction();

        try {
            Page loadPage = currentSession.load(Page.class, id);
            currentSession.delete(loadPage);
            currentSession.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeCurrentSessionWithTransaction();
        }
    }

    @Override
    public void deleteAllPages() {
        openCurrentSessionWithTransaction();

        List<Page> entityList = getAllPages();
        for (Page entity : entityList) {
            deletePage(entity.getId());
        }

        closeCurrentSessionWithTransaction();
    }

    @Override
    public boolean updatePage(int id, Page updatedPage) {
        openCurrentSessionWithTransaction();

        try {
            Page loadPage = currentSession.load(Page.class, id);
            loadPage.setPath(updatedPage.getPath());
            loadPage.setCode(updatedPage.getCode());
            loadPage.setContent(updatedPage.getContent());

            currentSession.update(loadPage);
            currentSession.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeCurrentSessionWithTransaction();
        }
    }


}
