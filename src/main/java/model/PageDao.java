package model;

import App.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.EntityNotFoundException;
import java.util.List;

public class PageDao {

    private Session currentSession;
    private Transaction currentTransaction;

    public PageDao() {
    }

    public Session openCurrentSession() {
        currentSession = HibernateSessionFactory.getSessionFactory().openSession();
        return currentSession;
    }

    public Session openCurrentSessionWithTransaction() {
        currentSession = HibernateSessionFactory.getSessionFactory().openSession();
        currentTransaction = currentSession.beginTransaction();
        return currentSession;
    }

    public void closeCurrentSession() {
        currentSession.close();
    }

    public void closeCurrentSessionWithTransaction() {
        currentTransaction.commit();
        currentSession.close();
    }


    public void createPage(Page page) {
        currentSession.save(page);
    }

    public boolean deletePage(int id) {
        try {
            Page loadPage = currentSession.load(Page.class, id);
            currentSession.delete(loadPage);
            currentSession.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Page> getAllPages() {
        return (List<Page>) currentSession.createQuery("from Page").list();
    }

    public model.Page getPage(int id) {
        return currentSession.get(Page.class, id);
    }

    public boolean updatePage(int id, Page updatedPage) {
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
        }
    }

    public void deleteAllPages() {
        List<Page> entityList = getAllPages();
        for (Page entity : entityList) {
            deletePage(entity.getId());
        }
    }

}
