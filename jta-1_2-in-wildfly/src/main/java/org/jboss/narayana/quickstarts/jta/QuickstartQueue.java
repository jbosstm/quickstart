/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstarts.jta;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.Transactional;

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
