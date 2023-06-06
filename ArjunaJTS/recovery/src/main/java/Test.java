import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class Test {
    private static String RECOVERY_STORE = AbstractExampleXAResource.DATA_DIR + "tx-object-store";
    /*
     * During a recovery pass we perform bottom up recovery where we ask the resources
     * for the Xids (transaction branches) they know about. To avoid attempting recovery
     * on resources that are about to be committed by an active (ie in flight) transaction
     * we back off for a certain period (controlled via a property called orphanSafetyInterval
     * ie it controls the amount of time to wait before deciding whether the Xid is orphaned
     * and needs to be committed). When bottom up recovery has finished we garbage collect
     * the Xids. Then, after waiting for a certain period (controlled va a property called
     * periodicRecoveryPeriod), we repeat the whole procedure. Therefore, if the
     * orphanSafetyInterval is longer than the periodicRecoveryPeriod then the Xid will never
     * be chosen as a candidate for recovery. In other words ensure that the the
     * orphanSafetyInterval is shorter than the periodicRecoveryPeriod.
     */
    private static final int PERIODIC_RECOVERY_PERIOD = 10; // the time unit is seconds
    private static final int ORPHAN_SAFETY_INTERVAL = 500 * PERIODIC_RECOVERY_PERIOD; // the time unit is milliseconds

    public static void main(String[] args) throws Exception {

        boolean crash = false;
        boolean recover = false;
        boolean auto = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-crash"))
                crash = true;
            if (args[i].equals("-recover"))
                recover = true;
            if (args[i].equals("-auto"))
                auto = true;
        }

        if (!crash && !recover) {
            System.err.println("You must specify either -crash or -recover");
        } else {
            // Perform some setup
            jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(
                    Arrays.asList(new String[] { "ExampleXAResourceRecovery" }));
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList(new String[] { "*" }));
            arjPropertyManager.getCoordinatorEnvironmentBean().setDefaultTimeout(0);
            recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(PERIODIC_RECOVERY_PERIOD);
            jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(ORPHAN_SAFETY_INTERVAL);

            arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(RECOVERY_STORE);

            ObjectStoreEnvironmentBean communicationStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator
                    .getNamedInstance(com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "communicationStore");
            communicationStoreObjectStoreEnvironmentBean.setObjectStoreDir(RECOVERY_STORE);

            ObjectStoreEnvironmentBean stateStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator
                    .getNamedInstance(com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "stateStore");
            stateStoreObjectStoreEnvironmentBean.setObjectStoreDir(RECOVERY_STORE);

            ORB myORB = ORB.getInstance("test");
            RootOA myOA = OA.getRootOA(myORB);

            myORB.initORB(args, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple rm
                = new com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple(true);

            if (!recover) {
                UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

                ut.begin();

                Transaction txImple = TransactionManager.transactionManager().getTransaction();

                txImple.enlistResource(new ExampleXAResource1());
                txImple.enlistResource(new ExampleXAResource2());

                ut.commit();
            } else {
                if (auto) {
                    // wait long enough for recovery (80 seconds should suffice)
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Runtime.getRuntime().halt(0);
                        }
                    }, 80000);
                }

                System.out.print("Press enter after recovery is complete to shutdown: ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                reader.readLine();
                Runtime.getRuntime().halt(0);
            }
        }
    }
}