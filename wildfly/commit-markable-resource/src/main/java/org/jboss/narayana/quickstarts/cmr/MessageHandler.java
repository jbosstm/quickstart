package org.jboss.narayana.quickstarts.cmr;

import java.util.Optional;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.transaction.Transactional;

/**
 * Helper class used for sending and receiving messages
 * to the attached jms queue. Running it as XA participant.
 */
@Dependent
public class MessageHandler {

    @Resource(lookup = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:/queue/cmr")
    private Queue queue;

    @Transactional(Transactional.TxType.MANDATORY)
    public void send(final String message) {
        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(queue, message);
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Optional<String> get() throws JMSException {
        try (JMSContext context = connectionFactory.createContext()) {
            Message msg = context.createConsumer(queue).receive(500);
            if(msg == null) return Optional.empty();
            if(msg instanceof TextMessage) return Optional.of(((TextMessage)msg).getText());
            throw new IllegalStateException("Expected message " + msg + " being type of text but it's "
                + msg.getJMSType());
        }
    }
}