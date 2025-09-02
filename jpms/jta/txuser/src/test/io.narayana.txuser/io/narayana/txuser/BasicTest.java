package io.narayana.txuser;

import com.arjuna.ats.arjuna.common.MetaObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BasicTest {
    private TxUser txUser;

    @BeforeClass
    public static void beforeClass() {
        MetaObjectStoreEnvironmentBean storeConfig =
                BeanPopulator.getDefaultInstance(MetaObjectStoreEnvironmentBean.class);
        storeConfig.setObjectStoreDir("target");
    }

    @Before
    public void before() {
        txUser = new TxUser();
    }

    @Test
    public void testCommit() {
        try {
            TestResource xar1 = new TestResource();
            TestResource xar2 = new TestResource();
            UserTransaction utx = txUser.startTransaction();

            txUser.enlistResources(utx, xar1, xar2);
            txUser.endTransaction(utx);

            assertEquals(1, xar1.commitCount);
            assertEquals(1, xar2.commitCount);
        } catch (TxUserException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRollback() {
        boolean failPrepareFirstResource = true;
        TestResource xar1 = new TestResource();
        TestResource xar2 = new TestResource(failPrepareFirstResource);
        UserTransaction utx = null;

        try {
            utx = txUser.startTransaction();
            txUser.enlistResources(utx, xar1, xar2);
            txUser.endTransaction(utx);
        } catch (TxUserException e) {
            assertEquals(1, xar1.rollbackCount);
            assertEquals(1, xar2.rollbackCount);
            assertNotNull(utx);
            try {
                assertEquals(Status.STATUS_NO_TRANSACTION, txUser.getStatus(utx));
            } catch (TxUserException ex) {
                fail("Cannot get transaction status: " + e.getMessage());
            }
        }
    }
}
