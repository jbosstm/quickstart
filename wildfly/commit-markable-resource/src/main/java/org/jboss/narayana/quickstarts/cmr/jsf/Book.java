package org.jboss.narayana.quickstarts.cmr.jsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import org.jboss.narayana.quickstarts.cmr.BookProcessorCmr;

/**
 * used at jsf page <code>index.xhtml</code> placed at <code>src/main/webapp</code> 
 */
@Named
@RequestScoped
public class Book {
    private String title;

    @Inject
    private BookProcessorCmr bookProcessor;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Transactional
    public void submit() {
        System.out.println("To save book with title: " + this.title);
        bookProcessor.fileBook(this.title);
    }
}