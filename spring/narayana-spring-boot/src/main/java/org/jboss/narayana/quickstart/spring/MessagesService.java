package org.jboss.narayana.quickstart.spring;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Service to send messages to the JMS queue.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
public class MessagesService {

    public final static String QUEUE_NAME = "test-messages";
    private final Logger logger = LoggerFactory.getLogger(MessagesService.class);
    private final List<String> receivedMessages = new LinkedList<>();
    private final JmsTemplate jmsTemplate;

    public MessagesService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void sendMessage(String message) {
        this.logger.info("Sending message '{}' to '{}' queue", message, QUEUE_NAME);
        this.jmsTemplate.convertAndSend(QUEUE_NAME, message);
    }

    public List<String> getReceivedMessages() {
        this.logger.info("Returning received messages '{}'", this.receivedMessages);
        return this.receivedMessages;
    }

    public void clearReceivedMessages() {
        this.receivedMessages.clear();
    }

    @JmsListener(destination = QUEUE_NAME)
    public void onMessage(String message) {
        this.logger.info("Received message '{}'", message);
        this.receivedMessages.add(message);
    }
}