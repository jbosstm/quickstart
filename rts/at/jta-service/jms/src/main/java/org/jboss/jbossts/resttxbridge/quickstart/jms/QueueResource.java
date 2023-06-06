package org.jboss.jbossts.resttxbridge.quickstart.jms;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XASession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * 
 */
@Path("/")
@Stateless
public class QueueResource {

    @Resource(mappedName = "java:/JmsXA")
    private XAConnectionFactory xaConnectionFactory;

    @Resource(mappedName = "java:/queue/resttx")
    private Queue queue;

    @POST
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Response sendNotification(@QueryParam("message") String message) throws Exception {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    session.close();
                } catch (JMSException e) {
                    System.out.println("Error closing JMS connection: " + e.getMessage());
                }
            }
        }

        return Response.ok().build();
    }

    @GET
    public String getMessage() throws JMSException {
        XAConnection connection = xaConnectionFactory.createXAConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(queue);
        connection.start();

        TextMessage message = (TextMessage) consumer.receive(5000);

        connection.close();
        session.close();

        if (message != null)
            return message.getText();

        return null;
    }

}