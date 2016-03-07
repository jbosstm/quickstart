/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.spring;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.util.List;

/**
 * Quickstart execution service.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
public class QuickstartService {

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private EntriesService entriesService;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private RecoveryManagerService recoveryManagerService;

    /**
     * Write entry to the database, send JMS message, and commit the transaction.
     *
     * @param entry String entry to be saved to the database.
     * @throws Exception
     */
    public void demonstrateCommit(String entry) throws Exception {
        executeDemonstration(entry, transactionManager::commit, null);
    }

    /**
     * Write entry to the database, send JMS message, and rollback the transaction.
     *
     * @param entry String entry to be saved to the database.
     * @throws Exception
     */
    public void demonstrateRollback(String entry) throws Exception {
        executeDemonstration(entry, transactionManager::rollback, null);
    }

    /**
     * Write entry to the database, send JMS message, and crash application.
     *
     * @param entry String entry to be saved to the database.
     * @throws Exception
     */
    public void demonstrateCrash(String entry) throws Exception {
        executeDemonstration(entry, transactionManager::commit, new DummyXAResource(true));
    }

    /**
     * Execute a recovery to complete transactions interrupted by the previous crash.
     *
     * @throws Exception
     */
    public void demonstrateRecovery() throws Exception {
        List<Entry> entriesBefore = entriesService.getAll();
        System.out.println("Entries at the start: " + entriesBefore);
        recoveryManagerService.addXAResourceRecovery(new DummyXAResourceRecovery());
        waitForRecovery(entriesBefore);
        System.out.println("Entries at the end: " + entriesService.getAll());
    }

    /**
     * Execute one of the quickstart scenarios.
     *
     * @param entry String entry to be saved to the database.
     * @param terminator Action with which transaction should be terminated.
     * @param xaResource XAResource to enlist to the transaction.
     * @throws Exception
     */
    private void executeDemonstration(String entry, TransactionTerminator terminator, XAResource xaResource) throws Exception {
        System.out.println("Entries at the start: " + entriesService.getAll());

        transactionManager.begin();
        if (xaResource != null) {
            transactionManager.getTransaction().enlistResource(xaResource);
        }
        entriesService.create(entry);
        messagesService.send("Created entry '" + entry + "'");
        terminator.terminate();

        System.out.println("Entries at the end: " + entriesService.getAll());
    }

    /**
     * Wait until entry is recovered. Method waits maximum for 15 seconds. But normally it should finish in 5 seconds.
     *
     * @param entriesBefore List of entries in the database before the recovery.
     * @throws Exception In case the recovery fails, exception is thrown.
     */
    private void waitForRecovery(List<Entry> entriesBefore) throws Exception {
        boolean isComplete = false;

        for (int i = 0; i < 3 && !isComplete; i++) {
            sleep(5000);
            isComplete = entriesBefore.size() < entriesService.getAll().size();
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
