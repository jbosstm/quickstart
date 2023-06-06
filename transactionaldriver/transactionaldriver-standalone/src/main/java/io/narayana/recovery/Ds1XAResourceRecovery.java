package io.narayana.recovery;

import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import io.narayana.util.DBUtils;

/**
 * Simple {@link XAResourceRecovery} class which provides {@link XAResource}
 * for specific log in data. In this case it's just for particular database.
 * <br>
 * Still this helps {@link XARecoveryModule} to check in-doubt transaction at the database side
 * and try to match them to transaction in the Narayana transaction log store.
 */
public class Ds1XAResourceRecovery implements XAResourceRecovery {

    private XAConnection xaConn;
    private boolean wasReturned = false;

    @Override
    public XAResource getXAResource() throws SQLException {
        if(xaConn == null) {
            xaConn = DBUtils.getXADatasource(DBUtils.DB_1).getXAConnection();
        }
        return xaConn.getXAResource();
    }

    @Override
    public boolean initialise(String p) throws SQLException {
        return true;
    }

    @Override
    public boolean hasMoreResources() {
        wasReturned = !wasReturned;
        return wasReturned;
    }

}