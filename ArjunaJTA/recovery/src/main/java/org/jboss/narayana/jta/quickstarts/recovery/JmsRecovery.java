package org.jboss.narayana.jta.quickstarts.recovery;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.service.extensions.xa.recovery.ActiveMQXAResourceRecovery;
import org.jboss.narayana.jta.quickstarts.util.DummyXAResource;
import org.jboss.narayana.jta.quickstarts.util.Util;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnection;
import jakarta.jms.XASession;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

public class JmsRecovery extends RecoverySetup {
    private static EmbeddedActiveMQ jmsServer;
    private static ActiveMQConnectionFactory xacf;
    private static Queue queue;
    private static boolean inVM = true;

    public static void main(String[] args) throws Exception {

        if (args.length == 1) {
            startServices();
            try {
                if (args[0].equals("-f")) {
                    new JmsRecovery().testXAWithErrorPart1();
                } else if (args[0].equals("-r")) {
                    startRecovery();
                    new JmsRecovery().testXAWithErrorPart2();
                } else {
                    printErrorMessage();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            stopServices();
        }else {
            printErrorMessage();
        }

    }

    private static void printErrorMessage() {
        System.err.println("to generate something to recover: java JmsRecovery -f");
        System.err.println("to recover from the failure: java JmsRecovery -r");
    }

    public static void startServices() throws Exception {
        startActiveMQ();
        startRecovery();
    }

    public static void stopServices() throws Exception {
        stopRecovery();
        stopActiveMQ();
    }

    private static void startActiveMQ() throws Exception {
        /*
         * Step 1. Decide whether to use inVM or remote communications: clients connect
         * to servers by obtaining connections from a ConnectorFactory servers accept
         * connections from clients by obtaining acceptors from an AcceptorFactory
         */
        String acceptorName = inVM ? InVMAcceptorFactory.class.getName() : NettyAcceptorFactory.class.getName();
        String connFacName = inVM ? InVMConnectorFactory.class.getName() : NettyConnectorFactory.class.getName();

        startActiveMQServer(acceptorName, connFacName);
        initialiseActiveMQClient(connFacName);
    }

    private static void stopActiveMQ() throws Exception {
        xacf.close();
        jmsServer.stop();
        System.out.println("Waiting some seconds to stop the server...");
    }

    public static void startRecovery() {
        RecoverySetup.startRecovery();
    }

    private static void startActiveMQServer(String acceptorName, String connFacName) throws Exception {
        // Create the server configuration
        Configuration configuration = new ConfigurationImpl();

        configuration.setPersistenceEnabled(true);
        configuration.setSecurityEnabled(false);
        configuration.setJournalType(JournalType.NIO);
        configuration.setJournalDirectory(Util.activeMQStoreDir);
        configuration.setBindingsDirectory(Util.activeMQStoreDir + "/bindings");
        configuration.setLargeMessagesDirectory(Util.activeMQStoreDir + "/largemessages");

        configuration.getAcceptorConfigurations().add(new TransportConfiguration(acceptorName));
        configuration.getConnectorConfigurations().put("connector", new TransportConfiguration(connFacName));

        // Start the JMS Server using the ActiveMQ core server
        jmsServer = new EmbeddedActiveMQ();
        jmsServer.setConfiguration(configuration);
        jmsServer.start();

        System.out.println("Embedded JMS Server is running");
    }

    // Initialise client side objects: connection factory and JMS queue
    private static void initialiseActiveMQClient(String connFacName) {
        xacf = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.XA_CF,
                new TransportConfiguration(connFacName));
        try (Session s = xacf.createConnection().createSession()) {
            queue = s.createQueue("TestQueue");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startJTATx(XAResource... resources)
            throws XAException, SystemException, NotSupportedException, RollbackException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        for (XAResource xaRes : resources)
            tm.getTransaction().enlistResource(xaRes);
    }

    private void endJTATx(boolean commit)
            throws SystemException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // no need to delist resources since that is done automatically by a JTA compliant TM
        if (commit)
            tm.commit();
        else
            tm.rollback();
    }

    private void produceMessages(XAConnection connection, String... msgs) throws Exception {
        // Create an XA session and a message producer
        try(Session session = connection.createXASession();
                MessageProducer producer = session.createProducer(queue);){
            for (String msg : msgs)
                producer.send(session.createTextMessage(msg));
        }
    }

    public void produceMessages(DummyXAResource.faultType fault, String... messages) throws Exception {
        try(XAConnection connection = xacf.createXAConnection();) {
            connection.start();

            // Begin some Transaction work
            XASession xaSession = connection.createXASession();
            XAResource xaRes = xaSession.getXAResource();

            startJTATx(new DummyXAResource(fault), xaRes);

            produceMessages(connection, messages);

            endJTATx(true);
        }
    }

    private int consumeMessages(MessageConsumer consumer, long millis, int cnt) throws JMSException {
        for (int i = 0; i < cnt; i++) {
            TextMessage tm = (TextMessage) consumer.receive(millis);
            if (tm == null)
                return i;

            System.out.println("Message received: " + tm.getText());
        }

        return cnt;
    }

    public int consumeMessages(int cnt, long millis) throws Exception {
        int msgCnt = 0;

        try(XAConnection connection = xacf.createXAConnection();
                XASession xaSession = connection.createXASession();
                MessageConsumer xaConsumer = xaSession.createConsumer(queue);) {
            connection.start();

            // create an XA JMS session and enlist the corresponding XA resource within a transaction
            XAResource xaRes = xaSession.getXAResource();

            startJTATx(xaRes, new DummyXAResource(DummyXAResource.faultType.NONE));

            // consume 2 messages withing a transaction
            msgCnt = consumeMessages(xaConsumer, millis, cnt);

            // roll back the transaction - since we consumed the messages inside a
            // transaction they should still be available
            endJTATx(true);
        }

        return msgCnt;
    }

    public void drainQueue() throws Exception {
        while (consumeMessages(100, 500) == 100)
            System.out.println("drained 100 messages");
    }

    public void testXASendWithoutError() throws Exception {
        drainQueue();
        produceMessages(DummyXAResource.faultType.NONE, "hello", "world");
    }

    public void testXARecvWithoutError() throws Exception {
        int cnt = consumeMessages(2, 500);
        if (cnt != 2)
            throw new RuntimeException("Expected 2 messages but received " + cnt);
    }

    public void testXAWithErrorPart1() throws Exception {
        // drain the queue before producing messages
        drainQueue();

        produceMessages(DummyXAResource.faultType.HALT, "hello", "world");
        throw new RuntimeException("The commit request should have halted the VM");
    }

    public void testXAWithErrorPart2() throws Exception {
        runRecoveryScan();
        int cnt = consumeMessages(2, 500);
        if (cnt != 2)
            throw new RuntimeException("Expected 2 messages but received " + cnt);

        System.out.println("Message Count after running recovery: " + cnt);
    }
}