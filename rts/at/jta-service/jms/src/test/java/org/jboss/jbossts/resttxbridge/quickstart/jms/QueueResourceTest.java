package org.jboss.jbossts.resttxbridge.quickstart.jms;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;

/**
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * 
 */
@RunWith(Arquillian.class)
@ServerSetup(value = QueueResourceTest.ServerQueueResourceSetup.class)
public class QueueResourceTest {

    public static class ServerQueueResourceSetup implements ServerSetupTask {

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient
                    .online(OnlineOptions.standalone().wrap(managementClient.getControllerClient()));
            try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute(
                        "/subsystem=messaging-activemq/server=default/jms-queue=\"resttx\":add(entries=[java:/queue/resttx])");
            }
            new Administration(creaper).reload();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient
                    .online(OnlineOptions.standalone().wrap(managementClient.getControllerClient()));
            try (FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=messaging-activemq/server=default/jms-queue=\"resttx\":remove()");
            }
        }
    }

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
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/web.xml"))
                .addPackages(true, "org.jboss.jbossts.resttxbridge.quickstart.jms")
                .addPackages(true, "jakarta.ws.rs.core")
                .addPackages(true, "jakarta.ws.rs.ext")
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
        final Link participantEnlistmentLink = Link.fromUri(txSupport.getDurableParticipantEnlistmentURI())
                .title(TxLinkNames.PARTICIPANT).rel(TxLinkNames.PARTICIPANT).type(TxMediaType.PLAIN_MEDIA_TYPE).build();

        System.out.println("Sending message...");
        final Response response = ClientBuilder.newClient().target(DEPLOYMENT_URL)
                .queryParam(MESSAGE_QUERY_PARAMETER, message).request()
                .header("Link", participantEnlistmentLink).post(null);
        Assert.assertEquals(200, response.getStatus());
    }

    private String getMessage() throws Exception {
        System.out.println("Getting message...");
        final String response = ClientBuilder.newClient().target(DEPLOYMENT_URL).request().get(String.class);

        System.out.println("Received message: " + response);

        return response;
    }
}