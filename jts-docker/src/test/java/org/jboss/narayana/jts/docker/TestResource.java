package org.jboss.narayana.jts.docker;

import com.arjuna.ats.internal.jts.ORBManager;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
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