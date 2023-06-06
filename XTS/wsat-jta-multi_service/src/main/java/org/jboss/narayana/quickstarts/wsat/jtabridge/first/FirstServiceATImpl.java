package org.jboss.narayana.quickstarts.wsat.jtabridge.first;

import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;

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
@Remote(FirstServiceAT.class)
@WebService(serviceName = "FirstServiceATService", portName = "FirstServiceAT", name = "FirstServiceAT", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/first")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class FirstServiceATImpl implements FirstServiceAT {

    private static final int ENTITY_ID = 1;

    @PersistenceContext
    protected EntityManager em;

    /**
     * Incriment the first counter. This is done by updating the counter within a JTA transaction. The JTA transaction
     * was automatically bridged from the WS-AT transaction.
     */
    @WebMethod
    public void incrementCounter(int num) {

        System.out.println("[SERVICE] First service invoked to increment the counter by '" + num + "'");

        // invoke the backend business logic:
        System.out.println("[SERVICE] Using the JPA Entity Manager to update the counter within a JTA transaction");

        FirstCounterEntity entityFirst = lookupCounterEntity();
        entityFirst.incrementCounter(num);
        em.merge(entityFirst);
    }

    @WebMethod
    public int getCounter() {
        System.out.println("[SERVICE] getCounter() invoked");
        FirstCounterEntity firstCounterEntity = lookupCounterEntity();
        if (firstCounterEntity == null) {
            return -1;
        }
        return firstCounterEntity.getCounter();
    }

    @WebMethod
    public void resetCounter() {
        FirstCounterEntity entityFirst = lookupCounterEntity();
        entityFirst.setCounter(0);
        em.merge(entityFirst);
    }

    private FirstCounterEntity lookupCounterEntity() {
        FirstCounterEntity entityFirst = em.find(FirstCounterEntity.class, ENTITY_ID);
        if (entityFirst == null) {
            entityFirst = new FirstCounterEntity(ENTITY_ID, 0);
            em.persist(entityFirst);
        }
        return entityFirst;
    }

}