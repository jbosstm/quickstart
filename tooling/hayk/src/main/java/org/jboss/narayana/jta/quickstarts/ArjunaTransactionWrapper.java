package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

import java.util.ArrayList;
import java.util.Collection;

public class ArjunaTransactionWrapper extends ArjunaTransactionImple {
    String type;
    byte[] gtid;

    private Collection<JTSXAResourceRecordWrapper> participants = new ArrayList<JTSXAResourceRecordWrapper>();

    public ArjunaTransactionWrapper (String type, Uid uid) {
        super(uid);

        this.type = type;
    }

    public String getType() {
        return type;
    }

    public byte[] getGtid() {
        return gtid;
    }

    public Uid getUid() {
        return get_uid();
    }

    public void add(JTSXAResourceRecordWrapper xar) {
        participants.add(xar);
        gtid = xar.getGlobalTransactionId();
    }

    public Collection<JTSXAResourceRecordWrapper> getParticipants() {
        return participants;
    }
}
