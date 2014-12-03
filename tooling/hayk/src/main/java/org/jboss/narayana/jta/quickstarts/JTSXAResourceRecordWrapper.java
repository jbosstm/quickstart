package org.jboss.narayana.jta.quickstarts;


import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * Extension of an XAResource record for exposing the underlying XAResource which is protected
 */
public class JTSXAResourceRecordWrapper extends com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord {
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

            // not required
            //return super.restoreState(os);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

