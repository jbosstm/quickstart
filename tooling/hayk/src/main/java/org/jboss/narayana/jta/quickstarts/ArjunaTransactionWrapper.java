package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

import javax.transaction.xa.Xid;

public class ArjunaTransactionWrapper extends ArjunaTransactionImple {
    XidWrapper xidWrapper;
    Xid xid;
    String type;

    public ArjunaTransactionWrapper (String type, Uid uid) {
        super(uid);

        this.xidWrapper = new XidWrapper(objectUid);
        this.xid = XidUtils.getXid(objectUid, false);

        this.type = type;
    }

    public Xid getXid() {
        return xid;
    }

    public String getType() {
        return type;
    }

    public XidWrapper getXidWrapper() {
        return xidWrapper;
    }
}
