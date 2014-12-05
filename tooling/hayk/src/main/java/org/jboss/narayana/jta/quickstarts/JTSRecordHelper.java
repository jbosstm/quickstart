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

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

public class JTSRecordHelper implements HayksJTSHelper {
    private static final String JTS_PARTICIPANT_REC_TYPE = "CosTransactions/XAResourceRecord";

    private static final String[] JTS_TYPES = new String[] {
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteHeuristicTransaction",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteTransaction",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteHeuristicServerTransaction",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteServerTransaction",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction",
            "StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/ServerTransaction/JCA"
    };

    private Map<Uid, ArjunaTransactionWrapper> txns = new HashMap<Uid, ArjunaTransactionWrapper>();
    private Collection<JTSXAResourceRecordWrapper> xarUids = new ArrayList<JTSXAResourceRecordWrapper>();

    private static Set<String> recordTypes = new HashSet<String> ();
    private ORB orb;
    private OA oa;

    public static void main(String[] args) throws Exception {
        //String storeDir = "/home/mmusgrov/source/forks/narayana/master/ArjunaJTS/jtax/target/test-classes/ObjectStore";
        //String storeDir = "/home/mmusgrov/source/forks/narayana/master/eap-tests-transactions/integration/jbossts/target/jbossas-jbossts/standalone/data/tx-object-store";
        //String storeDir = "/home/mmusgrov/source/forks/wildfly/wildfly/build/target/wildfly-9.0.0.Alpha2-SNAPSHOT-node2/standalone/data/tx-object-store";
        String storeDir = "/home/mmusgrov/Downloads/tx-object-store"; // the one gytis gave me

        JTSRecordHelper h = new JTSRecordHelper(storeDir);

        for (Map.Entry<Uid, ArjunaTransactionWrapper> e :h.txns.entrySet()) {
            Collection<JTSXAResourceRecordWrapper> participants = e.getValue().getParticipants();

            System.out.printf("\tParticipants of %s%n", h.convertXidBytes(e.getValue().getGtid()).toString());

            for (JTSXAResourceRecordWrapper w : participants)
                System.out.printf("\t\tgtid=%s bqual=%s heuristic=%d%n",
                        h.convertXidBytes(w.getGlobalTransactionId()), h.convertXidBytes(w.getBranchQualifier()),
                        w.getHeuristicValue());
        }
    }

    public JTSRecordHelper(String osDir) throws Exception {
        setupStore(osDir, false);
        initOrb();

        refresh();

        shutdown();
    }

    public void shutdown() {
        RecoveryManager.manager().terminate(true);
        StoreManager.shutdown();
        oa.destroy();
        orb.destroy();
    }

    public void refresh() throws ObjectStoreException, IOException {
        Collection<Uid> uids = getUids(JTS_PARTICIPANT_REC_TYPE);

        for (String type : JTS_TYPES)
            for (Uid uid : getUids(type))
                txns.put(uid, new ArjunaTransactionWrapper(type, uid));

        for (Uid xarUid : uids) {
            JTSXAResourceRecordWrapper wrapper = new JTSXAResourceRecordWrapper(xarUid);
            XidImple xid = (XidImple) wrapper.getXid();
            Uid txOfXar = new Uid(xid.getGlobalTransactionId());
            ArjunaTransactionWrapper txn = txns.get(txOfXar);

            xarUids.add(wrapper);

            if (txn != null)
                txn.add(wrapper);
        }
    }

    public Collection<JTSXAResourceRecordWrapper> getParticipants(ArjunaTransactionWrapper txn) {
        return txn.getParticipants();
    }

    public Map<Uid, ArjunaTransactionWrapper> getPendingJTSTxns() {
        return txns;
    }

    public Collection<JTSXAResourceRecordWrapper> getPendingXids() {
        return xarUids;
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

    private void setupStore(String storeDir, boolean hqstore) throws Exception {

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
    }

    private void initOrb() throws InvalidName {
        final Properties initORBProperties = new Properties();
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""
                + jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort());

        orb = ORB.getInstance("test");
        oa = OA.getRootOA(orb);

        orb.initORB(new String[] {}, initORBProperties);
        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
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

    public StringBuilder convertXidBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++)
            sb.append(bytes[i]);

        return sb;
    }
}
