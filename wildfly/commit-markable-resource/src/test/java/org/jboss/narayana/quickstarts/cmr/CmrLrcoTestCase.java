/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.narayana.quickstarts.cmr;


import java.util.Optional;
import java.util.Random;

import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 * Testcase shows the difference between behaviour of the LRCO and CMR.
 * <br>
 * We demonstrate here the particular case where LRCO fails to ensure
 * data consistency.
 */
@RunWith(Arquillian.class)
@ServerSetup(value = CmrLrcoTestCase.ServerCmrSetup.class)
@Ignore
//jakarta TODO: remove Ignore annotation and fix error NoClassDefFoundError: Ljakarta/transaction/TransactionManager
public class CmrLrcoTestCase {

    public static class ServerCmrSetup implements ServerSetupTask {
        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient.online(
                OnlineOptions.standalone().wrap(managementClient.getControllerClient()));

            try(FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/jdbc-cmr\":add()");
            }
            new Administration(creaper).reload();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient.online(
                    OnlineOptions.standalone().wrap(managementClient.getControllerClient()));

            try(FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/jdbc-cmr\":remove()");
            }
        }
    }

    @Inject
    private MessageHandler messageHandler;

    @Inject
    private BookProcessorLrco bookProcessorLrco;

    @Inject
    private BookProcessorCmr bookProcessorCmr;

    private TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "cmr.war")
            .addPackages(true, BookEntity.class.getPackage().getName())
            .addClass(ServerSetupTask.class)
            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
            .addAsResource("META-INF/cmr-create-script.sql", "META-INF/cmr-create-script.sql")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        war.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)  
            .importDirectory("src/main/webapp").as(GenericArchive.class),  
            "/", Filters.includeAll());

        System.out.printf(">>>>>>> webarchive content:%n%s%n", war.toString(true));
        return war;
    }

    @Before
    public void before() throws NamingException {
        transactionManager = (TransactionManager) new InitialContext().lookup("java:/jboss/TransactionManager");
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (final Throwable t) {
        }
    }

    @Test
    public void testLrcoCommit() throws Exception {
        final int entitiesCountBefore = bookProcessorLrco.getBooks().size();

        transactionManager.begin();
        int bookId = bookProcessorLrco.fileBook("test");
        transactionManager.commit();

        Assert.assertEquals("A new book should be filed",
            entitiesCountBefore + 1, bookProcessorLrco.getBooks().size());
        Optional<String> queueMessage = messageHandler.get();
        Assert.assertTrue("Expecting transaction being committed and message delivered", queueMessage.isPresent());
        Assert.assertEquals("The transaction was committed thus the inform message is expected to be received",
            BookProcessor.textOfMessage(bookId, "test"), queueMessage.get());
    }

    @Test
    public void testLrcoRollback() throws Exception {
        final int entitiesCountBefore = bookProcessorLrco.getBooks().size();

        transactionManager.begin();
        bookProcessorLrco.fileBook("test");
        transactionManager.rollback();

        Assert.assertEquals("Book filing was canceled no new book expected",
            entitiesCountBefore, bookProcessorLrco.getBooks().size());
        Assert.assertFalse("Sending the message was rolled back. No message expected.",
            messageHandler.get().isPresent());
    }

    @Test
    @BMRule(
        name = "Throw exception before prepare being finished simulating jvm crash with LRCO",
        condition = "NOT flagged(\"lrcoflag\")",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction", targetMethod = "save_state",
        action = "flag(\"lrcoflag\"), throw new java.lang.RuntimeException(\"byteman rules\")")
    public void testLrcoFailure() throws Exception {
        final int entitiesCountBefore = bookProcessorLrco.getBooks().size();
        String bookTitle = "test" + new Random().nextInt(999);

        transactionManager.begin();
        int bookId = bookProcessorLrco.fileBook(bookTitle);
        try {
        	transactionManager.commit();
        } catch (Exception re) {
        	if(transactionManager.getStatus() == Status.STATUS_ACTIVE)
        		transactionManager.rollback();
			if(!re.getMessage().equals("byteman rules"))
			    throw new IllegalStateException("test failed, not expected exception caught", re);
			// else: ignore as expected
		}

        // LRCO transaction consistency failure - if error happens just at the place before prepare phase
        // ends then the transaction consistency is in danger
        Assert.assertEquals("LRCO causes the book is saved even it should not be for transaction being consistent",
            entitiesCountBefore + 1, bookProcessorLrco.getBooks().size());
        Assert.assertEquals("Expected the id was persisted with the generated title but it's different",
            bookTitle, bookProcessorLrco.getBookById(bookId).getTitle());
        Assert.assertFalse("The transaction was rolled-back thus no message should be available",
            messageHandler.get().isPresent());
    }

    @Test
    public void testCmrCommit() throws Exception {
        final int entitiesCountBefore = bookProcessorCmr.getBooks().size();

        transactionManager.begin();
        int bookId = bookProcessorCmr.fileBook("test");
        transactionManager.commit();

        Assert.assertEquals("A new book should be filed",
            entitiesCountBefore + 1, bookProcessorCmr.getBooks().size());
        Optional<String> queueMessage = messageHandler.get();
        Assert.assertTrue("Expecting transaction being committed and message delivered", queueMessage.isPresent());
        Assert.assertEquals("The transaction was committed thus the inform message is expected to be received",
            BookProcessor.textOfMessage(bookId, "test"), queueMessage.get());
    }

    @Test
    public void testCmrRollback() throws Exception {
        final int entitiesCountBefore = bookProcessorCmr.getBooks().size();

        transactionManager.begin();
        bookProcessorCmr.fileBook("test");
        transactionManager.rollback();

        Assert.assertEquals("Book filing was canceled no new book expected",
            entitiesCountBefore, bookProcessorCmr.getBooks().size());
        Assert.assertFalse("Sending the message was rolled back. No message expected.",
            messageHandler.get().isPresent());
    }

    @Test
    @BMRule(
        name = "Throw exception before prepare being finished simulating jvm crash with CMR",
        condition = "NOT flagged(\"cmrflag\")",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction", targetMethod = "save_state",
        action = "flag(\"cmrflag\"), throw new java.lang.RuntimeException(\"byteman rules\")")
    public void testCmrWhileLrcoFailing() throws Exception {
        final int entitiesCountBefore = bookProcessorCmr.getBooks().size();

        transactionManager.begin();
        bookProcessorCmr.fileBook("test");
        try {
            transactionManager.commit();
        } catch (Exception re) {
            if(transactionManager.getStatus() == Status.STATUS_ACTIVE)
                transactionManager.rollback();
            if(!re.getMessage().equals("byteman rules"))
                throw new IllegalStateException("test failed, not expected exception caught", re);
            // else: ignore, which is the expected behaviour
        }

        Assert.assertEquals("Book filing was canceled no new book expected",
            entitiesCountBefore, bookProcessorCmr.getBooks().size());
        Assert.assertFalse("Sending the message was rolled back. No message expected.",
            messageHandler.get().isPresent());
    }
}
