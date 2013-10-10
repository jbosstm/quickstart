/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.wsat.jtabridge.first;

import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
