package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.Xid;

public class XidWrapper {
    XidImple xidImple = null;
    int heuristicValue;
    boolean committed = false;
    Uid uid;

    public XidWrapper(Uid uid) {
        this.uid = uid;
        heuristicValue = -1;
        xidImple = new XidImple(uid);
    }

    public XidWrapper(XidImple xidImple, Uid uid, int heuristicValue) {
        this.uid = uid;
        this.xidImple = xidImple;
        this.heuristicValue = heuristicValue;
    }

    public String getGlobalTransactionId() {
        return convertXidBytes(xidImple.getGlobalTransactionId()).toString();
    }


    public String getBranchQualifier() {
        return convertXidBytes(xidImple.getBranchQualifier()).toString();
    }

    public int getFormatId() {
        return xidImple.getFormatId();
    }

    public String getNodeName() {
        return XATxConverter.getNodeName(xidImple.getXID());
    }

    public int getHeuristicValue() {
        return heuristicValue;
    }

    public boolean isCommitted() { return committed; }

    static StringBuilder convertXidBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++)
            sb.append(bytes[i]);
        return sb;
    }
}