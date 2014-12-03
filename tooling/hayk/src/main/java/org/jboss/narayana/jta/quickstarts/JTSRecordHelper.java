/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2014,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.jta.quickstarts;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;

import javax.transaction.xa.Xid;

public class JTSRecordHelper implements HayksJTSHelper {
    private static ObjStoreBrowser osb;

    private static final String JTS_PARTICIPANT_REC_TYPE = "CosTransactions/XAResourceRecord";
    private static final String JTS_REC_TYPE = "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple";

    private static final String[] JTS_TYPES = new String[] {
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteHeuristicTransaction",
            "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteTransaction",
            "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteHeuristicServerTransaction",
            "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteServerTransaction",
            "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction",
            "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction/JCA"
    };

    private static Set<String> recordTypes = new HashSet<String> ();
    private Set<String> types;
    private Map<String, ArjunaTransactionWrapper> jtsTxns;
    private Collection<XidWrapper> xids;
    private Map<ArjunaTransactionWrapper, Collection<XidWrapper>> jtsTxnParticipants;

    /*
     * Hayks spec:
     * Read the xids of pending transactions. To check the count of pending transactions.
     * Read participant ids of pending transaction. To check the count of participants.
     * Read participant resources. To check participant status.
     */

    public static void main(String[] args) throws Exception {
        //String storeDir = "/home/mmusgrov/source/forks/narayana/master/ArjunaJTS/jtax/target/test-classes/ObjectStore";
        //String storeDir = "/home/mmusgrov/source/forks/narayana/master/eap-tests-transactions/integration/jbossts/target/jbossas-jbossts/standalone/data/tx-object-store";
        //String storeDir = "/home/mmusgrov/source/forks/wildfly/wildfly/build/target/wildfly-9.0.0.Alpha2-SNAPSHOT-node2/standalone/data/tx-object-store";
        String storeDir = "/home/mmusgrov/Downloads/tx-object-store"; // the one gytis gave me
        String nodeName = "1";

        JTSRecordHelper h = new JTSRecordHelper(nodeName, storeDir);

        System.out.printf("Types:%n");
        for (String type : h.types)
            System.out.printf("\t%s%n", type);

        System.out.printf("Pending xids:%n");
        for (XidWrapper w : h.xids) {
            System.out.printf("\tgtid=%s heuristic=%d%n", w.getGlobalTransactionId(), w.getHeuristicValue());
        }

        System.out.printf("Participants by txn:%n");
        for (Map.Entry<String, ArjunaTransactionWrapper> e : h.jtsTxns.entrySet()) {
            Collection<XidWrapper> participants = h.getParticipants(e.getValue());

            System.out.printf("\tParticipants of %s%n", e.getValue().getXidWrapper().getGlobalTransactionId());

            for (XidWrapper w : participants)
                System.out.printf("\t\tbqual=%s heuristic=%d%n", w.getBranchQualifier(), w.getHeuristicValue());
        }

        RecoveryManager.manager().terminate();
        StoreManager.shutdown();

    }

    public JTSRecordHelper(String nodeName, String osDir) throws Exception {
        jtsTxns = new HashMap<String, ArjunaTransactionWrapper>();
        xids = new ArrayList<XidWrapper>();
        jtsTxnParticipants = new HashMap<ArjunaTransactionWrapper, Collection<XidWrapper>> ();

        setupStore(nodeName, osDir, false);

        refresh();
    }

    public void refresh() throws Exception {
        types = getTypes();
        jtsTxns.clear();
        xids.clear();
        jtsTxnParticipants.clear();

        for (String type : JTS_TYPES)
            getPendingTxns(jtsTxns, type);

        getPendingXids(xids);

        for (ArjunaTransactionWrapper txn : jtsTxns.values()) {
            Xid txid = XATxConverter.getXid(txn.get_uid(), false, XATxConverter.FORMAT_ID);
            String gtid = convertXidBytes(txid.getGlobalTransactionId()).toString();

            for (XidWrapper xid : xids) {
                Collection<XidWrapper> participants = new ArrayList<XidWrapper>();

                if (gtid.equals(xid.getGlobalTransactionId()))
                    participants.add(xid);

                jtsTxnParticipants.put(txn, participants);
            }
        }
    }

    public Collection<XidWrapper> getParticipants(ArjunaTransactionWrapper txn) {
        return jtsTxnParticipants.get(txn);
    }

    public void getPendingJTSTxns(Map<String, ArjunaTransactionWrapper> txns) {
        txns.putAll(jtsTxns);
    }

    public void getPendingXids(Collection<XidWrapper> xids) {
        for (Uid uid : getUids(JTS_PARTICIPANT_REC_TYPE)) {
            JTSXAResourceRecordWrapper w = new JTSXAResourceRecordWrapper(uid);
            XidWrapper xidWrapper = new XidWrapper(w.xidImple, uid, w.heuristic);

            xids.add(xidWrapper);
        }
    }

    public Set<String> getTypes() throws Exception {
        recordTypes.clear();

        // if there is access to the physical store then use the direct store API:
        InputObjectState types = new InputObjectState();

        if (StoreManager.getRecoveryStore().allTypes(types)) {
            String typeName;

            do {
                try {
                    typeName = types.unpackString();
                    recordTypes.add(typeName);
                } catch (IOException e1) {
                    typeName = "";
                }
            } while (typeName.length() != 0);
        }

        return recordTypes;
    }


    private void getPendingTxns(Map<String, ArjunaTransactionWrapper> txns, String jtsRecType) {
        for (Uid uid : getUids(jtsRecType)) {
            ArjunaTransactionWrapper txn =  new ArjunaTransactionWrapper(jtsRecType, uid);
            XidWrapper xidWrapper = txn.getXidWrapper();

            txns.put(xidWrapper.getGlobalTransactionId(), txn);
        }
    }

    private void setupStore(String nodeName, String storeDir, boolean hqstore) throws Exception {
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryModuleClassNames(Arrays.asList(
                "com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule",
                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"
        ));

        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier(nodeName);
        BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class).setXaRecoveryNodes(Arrays.asList(new String[] {}));

        if (hqstore) {
            final String storeClassName =  com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor.class.getName();
            File hornetqStoreDir = new File(storeDir);

            BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class).setStoreDir(hornetqStoreDir.getCanonicalPath());

            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeClassName);
            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(storeDir);

            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);

        } else {
            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(storeDir);
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, null).setObjectStoreDir(storeDir);
        }

        //       RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT).suspend(true);

        if (osb != null)
            osb.stop();

        osb = new ObjStoreBrowser(storeDir);
        osb.setExposeAllRecordsAsMBeans(true);
        osb.start(); // only required if we want to use JMX
        // RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT).suspend(true);
    }

    private Collection<Uid> getUids(String type) {
        Collection<Uid> uids = new ArrayList<Uid>();
        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), type);

        while (true) {
            Uid u = iter.iterate();

            if (u == null || Uid.nullUid().equals(u))
                break;

            uids.add(u);
        }

        return uids;
    }

    private StringBuilder convertXidBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++)
            sb.append(bytes[i]);
        return sb;
    }
}
