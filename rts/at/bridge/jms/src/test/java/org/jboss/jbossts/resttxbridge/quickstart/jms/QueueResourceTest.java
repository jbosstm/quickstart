/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.jbossts.resttxbridge.quickstart.jms;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * 
 */
@RunWith(Arquillian.class)
public class QueueResourceTest {

    private static final String MANIFEST_STRING = "Manifest-Version: 1.0\n" + "Dependencies: org.jboss.narayana.rts\n";

    private static final String DEPLOYMENT_NAME = "restat-bridge-jms-test";

    private static final String BASE_URL = "http://localhost:8080/";

    private static final String TRANSACTION_MANAGER_URL = BASE_URL + "rest-at-coordinator/tx/transaction-manager";

    private static final String DEPLOYMENT_URL = BASE_URL + DEPLOYMENT_NAME;

    private static final String MESSAGE_QUERY_PARAMETER = "message";

    private static final String TEST_MESSAGE = "Hello World";

    private TxSupport txSupport;

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/test-jms.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/web.xml"))
                .addPackages(true, "org.jboss.jbossts.resttxbridge.quickstart.jms")
                .setManifest(new StringAsset(MANIFEST_STRING));

        return archive;
    }

    @Before
    public void before() {
        txSupport = new TxSupport(TRANSACTION_MANAGER_URL);
    }

    @After
    public void after() {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t) {
            // Ignore
        }
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("QueueResourceTest.testCommit()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        sendMessage(TEST_MESSAGE);

        System.out.println("Commiting REST-AT transaction...");
        txSupport.commitTx();

        Assert.assertEquals(TEST_MESSAGE, getMessage());
    }

    @Test
    public void testRollback() throws Exception {
        System.out.println("QueueResourceTest.testRollback()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        sendMessage(TEST_MESSAGE);

        System.out.println("Rolling back REST-AT transaction...");
        txSupport.rollbackTx();

        Assert.assertEquals(null, getMessage());
    }

    private void sendMessage(final String message) throws Exception {
        final Link participantEnlistmentLink = new Link(TxLinkNames.PARTICIPANT, TxLinkNames.PARTICIPANT,
                txSupport.getDurableParticipantEnlistmentURI(), null, null);

        System.out.println("Sending message...");
        final ClientResponse<String> response = new ClientRequest(DEPLOYMENT_URL)
                .queryParameter(MESSAGE_QUERY_PARAMETER, message).addLink(participantEnlistmentLink)
                .post(String.class);
        Assert.assertEquals(200, response.getStatus());
    }

    private String getMessage() throws Exception {
        System.out.println("Getting message...");
        final ClientResponse<String> response = new ClientRequest(DEPLOYMENT_URL).get(String.class);
        System.out.println("Received message: " + response.getEntity());

        return response.getEntity();
    }
}
