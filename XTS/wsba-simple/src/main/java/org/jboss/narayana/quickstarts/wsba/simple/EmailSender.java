/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.wsba.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of an email sender. This class simulates the sending of an email by outputting the message to the console.
 *
 * It also stores the mail in a list so that the tests can assert that the expected mail was 'sent'
 *
 * @author paul.robinson@redhat.com, 2012-05-02
 */
public class EmailSender {

    private static volatile List<String> mailBox = new ArrayList<String>();

    public static final String MAIL_TEMPLATE_CONFIRMATION = "Order confirmed";
    public static final String MAIL_TEMPLATE_CANCELLATION = "Order cancelled";

    public static void sendEmail(String emailAddress, String message) throws OrderServiceException {

        if (emailAddress.endsWith(".com")) {
            mailBox.add(message);
            System.out.println("[SERVICE] sent email: '" + message + "' to: '" + emailAddress + "'");
        } else {
            System.out.println("[SERVICE] Unable to send email due to an invalid address: '" + emailAddress + "'. We currently only support '.com' addresses");
            throw new OrderServiceException("Unable to send email due to an invalid address: '" + emailAddress + "'. We currently only support '.com' addresses");
        }

    }

    public static List<String> retrieveMail() {
        return mailBox;
    }
}
