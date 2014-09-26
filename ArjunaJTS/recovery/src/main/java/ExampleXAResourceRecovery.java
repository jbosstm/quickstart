/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
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
 * 
 * (C) 2005-2006,
 * @author Mark Little (mark.little@jboss.com)
 */

import java.sql.SQLException;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class ExampleXAResourceRecovery implements XAResourceRecovery {

    public XAResource getXAResource() throws SQLException {
        count++;

        if (count == 1)
            return new ExampleXAResource1(true);
        else
            return new ExampleXAResource2(true);
    }

    /**
     * Initialise with all properties required to create the resource(s).
     * 
     * @param p An arbitrary string from which initialization data is obtained.
     * 
     * @return <code>true</code> if initialization happened successfully, <code>false</code> otherwise.
     */

    public boolean initialise(String p) throws SQLException {
        return true;
    }

    /**
     * Iterate through all of the resources this instance provides access to.
     * 
     * @return <code>true</code> if this instance can provide more resources, <code>false</code> otherwise.
     */

    public boolean hasMoreResources() {
        boolean toReturn = false;

        if (count != 2)
            toReturn = true;
        else {
            // reset for next recovery scan

            count = 0;
        }

        return toReturn;
    }

    private int count = 0;

}
