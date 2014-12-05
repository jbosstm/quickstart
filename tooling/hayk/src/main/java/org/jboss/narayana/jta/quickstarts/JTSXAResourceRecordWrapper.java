package org.jboss.narayana.jta.quickstarts;


import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * Extension of an XAResource record for exposing the underlying XAResource which is protected
 */
public class JTSXAResourceRecordWrapper extends XARecoveryResourceImple { //com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord {
    int heuristic;
    boolean committed;
    XidImple xidImple;

    public JTSXAResourceRecordWrapper(Uid uid) {
        super(uid); // calls loadState which in turn calls restoreState
    }

    public boolean restoreState(InputObjectState os) {
        InputObjectState copy = new InputObjectState(os);
        try {
            heuristic = copy.unpackInt();
            committed = copy.unpackBoolean();
            xidImple = new XidImple(XidImple.unpack(copy));

            return super.restoreState(os);
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] getGlobalTransactionId() {
        return xidImple.getGlobalTransactionId();
    }


    public byte[] getBranchQualifier() {
        return xidImple.getBranchQualifier();
    }

    public int getFormatId() {
        return xidImple.getFormatId();
    }

    public String getNodeName() {
        return XATxConverter.getNodeName(xidImple.getXID());
    }

    public int getHeuristicValue() {
        return heuristic;
    }

    public boolean isCommitted() { return committed; }
}

