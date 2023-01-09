/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 */
package quickstart;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;

import java.io.ObjectInputStream;

// REST-AT transactions obey ACID properties and for a
// participant to be durable it must be serializable
// Participants are serialized if they successfully prepare
public class WorkDeserializer implements ParticipantDeserializer {
    @Override
    public Participant deserialize(ObjectInputStream objectInputStream) {
        try {
            Object object = objectInputStream.readObject();
            if (object instanceof Participant) {
                return (Participant) object;
            }
        } catch (Exception e) {
            System.err.printf("Cannot deserialize into Work: %s\n", e.getMessage());
        }

        return null;
    }

    @Override
    public Participant recreate(byte[] recoveryState) {
        return null;
    }

}
