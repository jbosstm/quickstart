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
package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.jaxrs.TaskResource;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.model.Task;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import java.io.StringReader;
import java.io.File;

/**
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * 
 */
@RunWith(Arquillian.class)
public class TaskResourceTest {

    private static final String MANIFEST_STRING = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.narayana.rts\n";

    private static final String DEPLOYMENT_NAME = "restat-bridge-jpa-test";

    private static final String BASE_URL = "http://localhost:8080/";

    private static final String TRANSACTION_MANAGER_URL = BASE_URL + "rest-at-coordinator/tx/transaction-manager";

    private static final String DEPLOYMENT_URL = BASE_URL + DEPLOYMENT_NAME;

    private static final String TASKS_URL = DEPLOYMENT_URL + "/" + TaskResource.TASKS_PATH_SEGMENT;

    private static final String USERS_URL = DEPLOYMENT_URL + "/" + TaskResource.USERS_PATH_SEGMENT;

    private static final String TEST_USERNAME = "gytis";

    private static final String TEST_TASK_TITLE1 = "task1";

    private static final String TEST_TASK_TITLE2 = "task2";

    private TxSupport txSupport;

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addPackage(TaskResource.class.getPackage())
                .addPackage(Task.class.getPackage())
                .addAsResource(new File("src/main/resources/META-INF/persistence.xml"), "META-INF/persistence.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/test-ds.xml"), "test-ds.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"), "web.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"), "beans.xml")
                .setManifest(new StringAsset(MANIFEST_STRING));

        return archive;
    }

    @Before
    public void before() {
        txSupport = new TxSupport(TRANSACTION_MANAGER_URL);
    }

    @After
    public void after() throws Exception {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t) {
            // Ignore
        }

        ClientBuilder.newClient().target(TASKS_URL).request().delete();
        ClientBuilder.newClient().target(USERS_URL).request().delete();
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("TaskResourceTest.testCommit()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        createTask(TEST_USERNAME, TEST_TASK_TITLE1);

        System.out.println("Commiting REST-AT transaction...");
        txSupport.commitTx();

        final JsonArray jsonArray = getUserTasks(TEST_USERNAME);

        Assert.assertEquals(1, jsonArray.size());
        Assert.assertEquals(TEST_USERNAME, jsonArray.getJsonObject(0).getString("owner"));
        Assert.assertEquals(TEST_TASK_TITLE1, jsonArray.getJsonObject(0).getString("title"));
    }

    @Test
    public void testRollback() throws Exception {
        System.out.println("TaskResourceTest.testRollback()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        createTask(TEST_USERNAME, TEST_TASK_TITLE1);

        System.out.println("Rolling back REST-AT transaction...");
        txSupport.rollbackTx();

        final JsonArray jsonArray = getUserTasks(TEST_USERNAME);

        Assert.assertEquals(0, jsonArray.size());
    }

    @Test
    public void testCommitWithTwoTasks() throws Exception {
        System.out.println("TaskResourceTest.testCommitWithTwoTasks()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        createTask(TEST_USERNAME, TEST_TASK_TITLE1);
        createTask(TEST_USERNAME, TEST_TASK_TITLE2);

        System.out.println("Commiting REST-AT transaction...");
        txSupport.commitTx();

        final JsonArray jsonArray = getUserTasks(TEST_USERNAME);

        Assert.assertEquals(2, jsonArray.size());
        Assert.assertEquals(TEST_USERNAME, jsonArray.getJsonObject(0).getString("owner"));
        Assert.assertEquals(TEST_TASK_TITLE1, jsonArray.getJsonObject(0).getString("title"));
        Assert.assertEquals(TEST_USERNAME, jsonArray.getJsonObject(1).getString("owner"));
        Assert.assertEquals(TEST_TASK_TITLE2, jsonArray.getJsonObject(1).getString("title"));
    }

    @Test
    public void testRollbackWithTwoTasks() throws Exception {
        System.out.println("TaskResourceTest.testRollbackWithTwoTasks()");

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        createTask(TEST_USERNAME, TEST_TASK_TITLE1);
        createTask(TEST_USERNAME, TEST_TASK_TITLE2);

        System.out.println("Rolling back REST-AT transaction...");
        txSupport.rollbackTx();

        final JsonArray jsonArray = getUserTasks(TEST_USERNAME);

        Assert.assertEquals(0, jsonArray.size());
    }

    private Response createTask(final String userName, final String title) throws Exception {
        System.out.println("Creating task " + title + " for user " + userName);

        final Link participantEnlistmentLink = Link.fromUri(txSupport.getDurableParticipantEnlistmentURI())
                .title(TxLinkNames.PARTICIPANT).rel(TxLinkNames.PARTICIPANT).type(TxMediaType.PLAIN_MEDIA_TYPE).build();

        Client client = ClientBuilder.newClient();

        Response response = client.target(TASKS_URL + "/" + userName + "/" + title).request()
                .header("Link", participantEnlistmentLink).post(null);
        Assert.assertEquals(201, response.getStatus());

        return response;
    }

    private JsonArray getUserTasks(final String userName) throws Exception {
        System.out.println("Getting all tasks of " + userName + "...");
        final String response = ClientBuilder.newClient().target(TASKS_URL + "/" + userName).request().get(String.class);
        final JsonArray jsonArray = Json.createReader(new StringReader(response)).readArray();

        System.out.println("Received tasks:");
        System.out.println(jsonArray);

        return jsonArray;
    }

}
