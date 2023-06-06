import java.sql.SQLException;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class ExampleXAResourceRecovery implements XAResourceRecovery {

    public XAResource getXAResource() throws SQLException {
        count++;

        if (count == 1)
            return new ExampleXAResource1(true);
        else
            return new ExampleXAResource2(true);
    }

    /**
     * Initialise with all properties required to create the resource(s).
     * 
     * @param p An arbitrary string from which initialization data is obtained.
     * 
     * @return <code>true</code> if initialization happened successfully, <code>false</code> otherwise.
     */

    public boolean initialise(String p) throws SQLException {
        return true;
    }

    /**
     * Iterate through all of the resources this instance provides access to.
     * 
     * @return <code>true</code> if this instance can provide more resources, <code>false</code> otherwise.
     */

    public boolean hasMoreResources() {
        boolean toReturn = false;

        if (count != 2)
            toReturn = true;
        else {
            // reset for next recovery scan
            count = 0;
        }

        return toReturn;
    }

    private int count = 0;

}