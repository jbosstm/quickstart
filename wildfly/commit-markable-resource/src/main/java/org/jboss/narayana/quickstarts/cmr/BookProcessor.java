package org.jboss.narayana.quickstarts.cmr;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.jboss.logging.Logger;

/**
 * Maintaining books in the system.
 */
public abstract class BookProcessor {
    private static final Logger log = Logger.getLogger(BookProcessor.class);

    @Inject
    private MessageHandler messageHandler;

    // define what entity manager to use
    protected abstract EntityManager getEntityManager();

    public static String textOfMessage(int id, String title) {
        return id + "#" + title;
    }

    @SuppressWarnings("unchecked")
    public List<BookEntity> getBooks() {
        final Query query = getEntityManager().createQuery("select book from " + BookEntity.class.getSimpleName() + " book");
        return (List<BookEntity>) query.getResultList();
    }

    public BookEntity getBookById(int id) {
        final Query query = getEntityManager().createQuery("select book from " + BookEntity.class.getSimpleName() + " book where id = :id");
        query.setParameter("id", id);
        return (BookEntity) query.getSingleResult();
    }

    public Integer fileBook(String title) {
        if(title == null) throw new NullPointerException("title");

        BookEntity book = new BookEntity().setTitle(title);
        Integer id = this.save(book);

        messageHandler.send(textOfMessage(id, title));

        log.infof("New book was filed as id: %s, title: %s", id, title);
        return id;
    }

    private Integer save(BookEntity book) {
        if (book.isTransient()) {
            getEntityManager().persist(book);
        } else {
            getEntityManager().merge(book);
        }
        return book.getId();
    }
}