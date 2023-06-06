package org.jboss.narayana.quickstart.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Service to send messages to the JMS queue.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
@Transactional
public class MessagesService {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public MessagesService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void send(String message) {
        jmsTemplate.convertAndSend(MessagesListener.QUEUE_NAME, message);
    }

}