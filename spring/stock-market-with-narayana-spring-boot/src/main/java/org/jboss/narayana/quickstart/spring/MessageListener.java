package org.jboss.narayana.quickstart.spring;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener listens for the messages in the "update" queue and writes them to the log.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Component
public class MessageListener {

    @JmsListener(destination = "updates")
    public void onMessage(String content) {
        System.out.println("----> " + content);
    }

}