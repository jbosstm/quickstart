package org.jboss.narayana.quickstarts.wsba.wsbaToJta;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author paul.robinson@redhat.com, 2012-05-03
 *
 * JPA entiry to represent the purchase order.
 */
@Entity
public class PurchaseOrder implements Serializable {

    private Integer id;
    private String item;
    private OrderStatus status;

    public PurchaseOrder() {

    }

    public PurchaseOrder(String item) {

        this.item = item;
        this.status = OrderStatus.PENDING;
    }

    @Id
    @GeneratedValue
    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getItem() {

        return item;
    }

    public void setItem(String item) {

        this.item = item;
    }

    public OrderStatus getStatus() {

        return status;
    }

    public void setStatus(OrderStatus status) {

        this.status = status;
    }
}