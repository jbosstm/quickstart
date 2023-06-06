package org.jboss.narayana.quickstart.spring;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener to receive messages from the JMS queue.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Component
public class MessagesListener {

    public static final String QUEUE_NAME = "quickstart-messages";

    @JmsListener(destination = QUEUE_NAME)
    public void onMessage(String message) {
        System.out.println("Message received: " + message);
        synchronized (QuickstartApplication.TO_WAIT) {
            QuickstartApplication.TO_WAIT.notify();
        }
    }

}