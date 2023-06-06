package org.jboss.narayana.quickstarts.jta;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XASession;
import jakarta.transaction.Transactional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Stateless
public class QuickstartQueue {

    @Resource(mappedName = "java:/JmsXA")
    private XAConnectionFactory xaConnectionFactory;

    @Resource(mappedName = "java:/queue/test")
    private Queue queue;

    @Transactional(Transactional.TxType.MANDATORY)
    public void send(final String message) {
        XAConnection connection = null;
        XASession session = null;

        try {
            connection = xaConnectionFactory.createXAConnection();
            session = connection.createXASession();
            MessageProducer messageProducer = session.createProducer(queue);

            connection.start();
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(message);

            messageProducer.send(textMessage);
            messageProducer.close();
        } catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }

                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Transactional
    public String get() {
        XAConnection connection = null;
        Session session = null;

        try {
            connection = xaConnectionFactory.createXAConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final MessageConsumer consumer = session.createConsumer(queue);

            connection.start();

            final TextMessage message = (TextMessage) consumer.receive(5000);

            if (message != null) {
                return message.getText();
            }

            return "";

        } catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }

                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}