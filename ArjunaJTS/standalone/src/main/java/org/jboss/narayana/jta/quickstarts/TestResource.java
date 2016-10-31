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
package org.jboss.narayana.jta.quickstarts;

import org.omg.CosTransactions.HeuristicHazard;
import com.arjuna.ats.internal.jts.ORBManager;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourcePOA;
import org.omg.CosTransactions.Vote;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestResource extends ResourcePOA {

    private final boolean doCommit;

    private final Resource reference;

    public TestResource(final boolean doCommit) {
        ORBManager.getPOA().objectIsReady(this);

        this.doCommit = doCommit;
        reference = org.omg.CosTransactions.ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference() {
        return reference;
    }

    public org.omg.CosTransactions.Vote prepare() throws SystemException, HeuristicMixed, HeuristicHazard {
        System.out.println("TestResource : prepare");

        if (doCommit) {
            System.out.println("\tTestResource : VoteCommit");

            return Vote.VoteCommit;
        } else {
            System.out.println("\tTestResource : VoteRollback");

            return Vote.VoteRollback;
        }
    }

    public void rollback() throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard {
        System.out.println("TestResource : rollback");
    }

    public void commit() throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard {
        System.out.println("TestResource : commit");
    }

    public void forget() throws SystemException {
        System.out.println("TestResource : forget");
    }

    public void commit_one_phase() throws SystemException, HeuristicHazard {
        System.out.println("TestResource : commit_one_phase");
    }
}
