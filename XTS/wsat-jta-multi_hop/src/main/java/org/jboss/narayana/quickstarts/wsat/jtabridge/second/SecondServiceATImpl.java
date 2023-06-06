package org.jboss.narayana.quickstarts.wsat.jtabridge.second;

import org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws.SecondServiceAT;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * @author paul.robinson@redhat.com, 2012-10-29
 */
@Stateless
@Remote(SecondServiceAT.class)
@WebService(serviceName = "SecondServiceATService", portName = "SecondServiceAT", name = "SecondServiceAT", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/second")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class SecondServiceATImpl implements SecondServiceAT {

    private static final int ENTITY_ID = 1;

    @PersistenceContext
    protected EntityManager em;

    /**
     * Incriment the second counter. This is done by updating the counter within a JTA transaction. The JTA transaction
     * was automatically bridged from the WS-AT transaction.
     */
    @WebMethod
    public void incrementCounter(int num) {

        System.out.println("[SERVICE_2] Second service invoked to increment the counter by '" + num + "'");

        // invoke the backend business logic:
        System.out.println("[SERVICE_2] Using the JPA Entity Manager to update the counter within a JTA transaction");

        SecondCounterEntity entitySecond = lookupCounterEntity();
        entitySecond.incrementCounter(num);
        em.merge(entitySecond);
    }

    @WebMethod
    public int getCounter() {
        System.out.println("[SERVICE_2] getFirstCounter() invoked");
        SecondCounterEntity secondCounterEntity = lookupCounterEntity();
        if (secondCounterEntity == null) {
            return -1;
        }
        return secondCounterEntity.getCounter();
    }

    @WebMethod
    public void resetCounter() {
        SecondCounterEntity entitySecond = lookupCounterEntity();
        entitySecond.setCounter(0);
        em.merge(entitySecond);
    }

    private SecondCounterEntity lookupCounterEntity() {
        SecondCounterEntity entitySecond = em.find(SecondCounterEntity.class, ENTITY_ID);
        if (entitySecond == null) {
            entitySecond = new SecondCounterEntity(ENTITY_ID, 0);
            em.persist(entitySecond);
        }
        return entitySecond;
    }

}