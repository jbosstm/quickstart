package io.narayana.demo;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import io.narayana.txuser.TxUser;
import io.narayana.txuser.TxUserException;
import io.narayana.config.Config;
import io.narayana.recovery.Recovery;

import io.narayana.recovery.RecoveryException;
import jakarta.transaction.UserTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.transaction.xa.XAResource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demonstration {

    static final Logger logger = LogManager.getLogger(Demonstration.class);

    static final Config config = new Config(
            "1",
            "target",
            1,
            1,
            List.of(
                    "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                    "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"
            )
    );

    static final Recovery recovery = new Recovery(config);
    static final TxUser txuser = new TxUser();

    public static void main(String[] args) throws Exception {
        recovery.start();

        Demonstration user = new Demonstration();
        AtomicBoolean committed = user.test2(recovery);

        recovery.stop();

        logger.info("recovered: {}", committed);
    }

    private XAResourceRecoveryHelper getXARRecoveryHelper(XAResource xar) {
        return new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] { xar };
            }
        };
    }

	private AtomicBoolean committed;

    public AtomicBoolean test2(Recovery recovery) throws InterruptedException, TxUserException, RecoveryException {
		committed = new AtomicBoolean(false);

		XAResource firstResource = new SimpleResource();
		final SimpleResourceXA_RETRY secondResource = new SimpleResourceXA_RETRY(this);

		// use helpers as an alternative to XAResourceRecovery
		recovery.addHelper(getXARRecoveryHelper(secondResource));

        List<String> uids;

        try {
            uids = recovery.lookupActions(Recovery.AtomicActionType);
        } catch (Error e) {
            logger.error("lookupActions", e);
            return committed;
        }

        int numberOfActions = uids.size();
        UserTransaction utx = txuser.startTransaction();

        txuser.enlistResources(utx, firstResource, secondResource);

        recovery.suspend();

        txuser.endTransaction(utx);

        logger.info("before scan recovered: {}", committed);
        try {
            // the unfinished transaction should be in the store
            uids = recovery.lookupActions(Recovery.AtomicActionType);
            if (numberOfActions == uids.size()) {
                logger.error("new action is not in the store%n");
            }
        } catch (RecoveryException e) {
            logger.error(e.getMessage());
        }

        recovery.resume();
        recovery.scan();

        if (!testFinish(this.committed)) {
            logger.info("resource still hasn't committed");
        }

        uids = recovery.lookupActions(Recovery.AtomicActionType);
        if (numberOfActions != uids.size()) {
            logger.info("new action is still in the store");
        }

        return committed;
    }

	public boolean testFinish(AtomicBoolean committed) throws InterruptedException {
		synchronized (this) {
			while (!committed.get()) {
				wait();
			}
		}

		return committed.get();
	}

    public synchronized void committed() {
        committed.set(true);
        notify();
    }
}
