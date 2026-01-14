package org.jboss.narayana.quickstarts.mongodb.simple;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.narayana.quickstarts.mongodb.simple.resources.BankingServiceJaxRs;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongodb.client.model.Filters.eq;

@RunAsClient
@ExtendWith(ArquillianExtension.class)
public class BankingServiceTest {

    @ArquillianResource
    public URL baseURL;

    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
        .withCreateContainerCmdModifier(cmd ->
            cmd.withHostConfig(cmd.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(27017),new ExposedPort(27017))))
        );

    private MongoClient mongo;

    private MongoCollection<Document> accounts;

    private Client client;

    @Deployment
    public static WebArchive createTestArchive() {

        //Use 'Shrinkwrap Resolver' to include the mongodb java driver in the deployment
        File lib = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.mongodb:mongo-java-driver").withoutTransitivity().asSingleFile();
        File compensations = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.jboss.narayana.compensations:compensations").withTransitivity().asSingleFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, BankingService.class.getPackage().getName())
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml")
                .addAsLibraries(lib).addAsLibraries(compensations)
                .addAsWebInfResource(
                    new StringAsset(
                        "<jboss-deployment-structure>\n" +
                            "  <deployment>\n" +
                            "    <dependencies>\n" +
                            "      <module name=\"org.jboss.xts\" />\n" +
                            "    </dependencies>\n" +
                            "  </deployment>\n" +
                            "</jboss-deployment-structure>"
                    ),
                    "jboss-deployment-structure.xml"
        );
        return archive;
    }

    @BeforeAll
    public static void initMongoDBContainer() {
        mongoDBContainer.start();
    }

    /**
     * Setup the initial test data. Give both accounts 'A' and 'B' £1000
     */
    @BeforeEach
    public void resetAccountData() {

        client = ClientBuilder.newClient();

        int port = mongoDBContainer.getMappedPort(27017);
        mongo = new MongoClient(mongoDBContainer.getHost(), mongoDBContainer.getMappedPort(27017));
        MongoDatabase database = mongo.getDatabase("test");

        database.getCollection("accounts").drop();
        accounts = database.getCollection("accounts");

        accounts.insertOne(new Document("name", "A").append("balance", 1000.0));
        accounts.insertOne(new Document("name", "B").append("balance", 1000.0));
    }

    @AfterEach
    public void closeClient() {
        mongo.close();
    }

    @AfterAll
    public static void stopContainer() {
        mongoDBContainer.stop();
    }

    /**
     * Transfer £100 from A to B and assert that it was successful.
     */
    @Test
    public void testSuccess() {
        invokeMoneyTransfer(BankingServiceJaxRs.ROOT_PATH, BankingServiceJaxRs.TRANSFER_MONEY, Response.Status.OK.getStatusCode(),
                "A", "B", 100.0);
        assertBalance("A", 900.0);
        assertBalance("B", 1100.0);
    }

    /**
     * Attempt to transfer £600 from A to B. The banking service will fail this transfer due to the amount being
     * above the transfer limit.
     *
     * The test asserts that both balances are set to £1000 after the transaction fails.
     */
    @Test
    public void testFailure() {

        //Initiate a 'high value' transfer that will fail
        invokeMoneyTransfer(BankingServiceJaxRs.ROOT_PATH, BankingServiceJaxRs.TRANSFER_MONEY, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "A", "B", 600.0);
        assertBalance("A", 1000.0);
        assertBalance("B", 1000.0);
    }

    /**
     * Simple helper method that requests a user's account document and asserts that the balance is as expected.
     *
     * @param account The account name, used to lookup the right account document.
     * @param expectedBalance The expected balance
     */
    private void assertBalance(String account, Double expectedBalance) {
        Document accountDoc = accounts.find(eq("name", account)).first();
        Double actualBalance = (Double) accountDoc.get("balance");
        Assertions.assertEquals(expectedBalance, actualBalance, 0, "Balance is not as expected. Got '" + actualBalance + "', expected: '" + expectedBalance + "'");
    }

    /**
     * Simple helper method that makes a JaxRs request to transfer money
     *
     * @param resourcePrefix Root path of the JaxRs application
     * @param resourcePath Resource path
     * @param expectedStatus Expected outcome (Response.Status code)
     * @param fromAccount Account to move money from
     * @param toAccount Account to move money to
     * @param amount Amount of the transfer
     */
    private void invokeMoneyTransfer(String resourcePrefix, String resourcePath, int expectedStatus, String fromAccount, String toAccount, double amount) {
        try {
            final Future<Response> future = client.target(baseURL.toURI())
                    .path(resourcePrefix)
                    .path(resourcePath)
                    .queryParam("fromAccount", fromAccount)
                    .queryParam("toAccount", toAccount)
                    .queryParam("amount", amount)
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .async()
                    .put(Entity.text(""));

            String entity = future.get(2, TimeUnit.SECONDS).readEntity(String.class);

            Assertions.assertEquals(expectedStatus, future.get().getStatus(), "response from " + resourcePrefix + "/" + resourcePath + " was " + entity);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Exception converting the URI: " + baseURL);
        } catch (ExecutionException | InterruptedException | TimeoutException e){
            throw new RuntimeException("Future action was timed out: " + e.getMessage());
        }
    }
}