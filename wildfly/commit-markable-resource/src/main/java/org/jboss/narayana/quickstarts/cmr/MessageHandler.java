/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.narayana.quickstarts.cmr;

import java.util.Optional;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.transaction.Transactional;

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
