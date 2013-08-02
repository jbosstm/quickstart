package org.jboss.narayana.quickstarts.nonTransactionalResource.bookService;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com 02/08/2013
 */
@CompensationScoped
public class InvoiceData implements Serializable {

    private Integer invoiceId;
    private String invoiceBody;

    public Integer getInvoiceId() {

        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {

        this.invoiceId = invoiceId;
    }

    public String getInvoiceBody() {

        return invoiceBody;
    }

    public void setInvoiceBody(String invoiceBody) {

        this.invoiceBody = invoiceBody;
    }
}