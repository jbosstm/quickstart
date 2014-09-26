/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
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
 * 
 * (C) 2005-2006,
 * @author Mark Little (mark.little@jboss.com)
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

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
            recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(10);

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

            com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple rm = new com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple(
                    true);

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
