/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.jbossts.resttxbridge.quickstart.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
