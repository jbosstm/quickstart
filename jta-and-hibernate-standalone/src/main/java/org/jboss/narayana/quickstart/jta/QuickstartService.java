/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.quickstart.jta;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class QuickstartService {

    @Inject
    private QuickstartEntityRepository quickstartEntityRepository;

    private TransactionManager transactionManager;

    public QuickstartService() throws NamingException {
        transactionManager = InitialContext.doLookup("java:/TransactionManager");
    }

    /**
     * Write value to the database, send JMS message, and commit the transaction.
     *
     * @param value String value to be saved to the database.
     * @throws Exception
     */
    public void demonstrateCommit(String value) throws Exception {
        executeDemonstration(value, transactionManager::commit, null);
    }

    /**
     * Write value to the database, send JMS message, and rollback the transaction.
     *
     * @param value String value to be saved to the database.
     * @throws Exception
     */
    public void demonstrateRollback(String value) throws Exception {
        executeDemonstration(value, transactionManager::rollback, null);
    }

    /**
     * Write value to the database, send JMS message, and crash application.
     *
     * @param value String value to be saved to the database.
     * @throws Exception
     */
    public void demonstrateCrash(String value) throws Exception {
        executeDemonstration(value, transactionManager::commit, new DummyXAResource(true));
    }

    /**
     * Execute a recovery to complete transactions interrupted by the previous crash.
     *
     * @throws Exception
     */
    public void demonstrateRecovery() throws Exception {
        List<QuickstartEntity> entriesBefore = quickstartEntityRepository.findAll();
        System.out.println("Entries at the start: " + entriesBefore);
        RecoveryManager.manager().startRecoveryManagerThread();
        waitForRecovery(entriesBefore);
        System.out.println("Entries at the end: " + quickstartEntityRepository.findAll());
    }

    /**
     * Execute one of the quickstart scenarios.
     *
     * @param value String value to be saved to the database.
     * @param terminator Action with which transaction should be terminated.
     * @param xaResource XAResource to enlist to the transaction.
     * @throws Exception
     */
    private void executeDemonstration(String value, TransactionTerminator terminator, XAResource xaResource) throws Exception {
        System.out.println("Entries at the start: " + quickstartEntityRepository.findAll());

        transactionManager.begin();
        if (xaResource != null) {
            transactionManager.getTransaction().enlistResource(xaResource);
        }
        quickstartEntityRepository.save(new QuickstartEntity(value));
        terminator.terminate();

        System.out.println("Entries at the end: " + quickstartEntityRepository.findAll());
    }

    /**
     * Wait until entity is recovered. Method waits maximum for 15 seconds. But normally it should finish in 5 seconds.
     *
     * @param entriesBefore List of entries in the database before the recovery.
     * @throws Exception In case the recovery fails, exception is thrown.
     */
    private void waitForRecovery(List<QuickstartEntity> entriesBefore) throws Exception {
        boolean isComplete = false;

        for (int i = 0; i < 3 && !isComplete; i++) {
            sleep(5000);
            isComplete = entriesBefore.size() < quickstartEntityRepository.findAll().size();
        }

        if (isComplete) {
            System.out.println("Recovery completed successfully");
        } else {
            throw new Exception("Something wrong happened and recovery didn't complete");
        }
    }

    /**
     * Sleep the thread for the number of milliseconds.
     *
     * @param millis
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Functional interface to implement desired transaction termination action.
     */
    private interface TransactionTerminator {

        void terminate() throws Exception;

    }

}
