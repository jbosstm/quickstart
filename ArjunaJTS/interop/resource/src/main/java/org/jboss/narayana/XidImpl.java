package org.jboss.narayana;

import javax.transaction.xa.Xid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

public class XidImpl implements Xid, Serializable {
    private int formatId;
    byte[] globalTransactionId;
    byte[] branchQualifier;

    public XidImpl(Xid xid) {
        formatId = xid.getFormatId();
        globalTransactionId = xid.getGlobalTransactionId();
        branchQualifier = xid.getBranchQualifier();
    }

    public XidImpl(String xid) {
        try {
            XidImpl o = (XidImpl) XidImpl.fromString(xid);

            formatId = o.getFormatId();
            globalTransactionId = o.getGlobalTransactionId();
            branchQualifier = o.getBranchQualifier();
        } catch (Exception e) {
            System.out.printf("XidImpl: deserialize error %s%n", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XidImpl)) return false;

        XidImpl xid = (XidImpl) o;

        if (formatId != xid.formatId) return false;
        if (!Arrays.equals(branchQualifier, xid.branchQualifier)) return false;
        if (!Arrays.equals(globalTransactionId, xid.globalTransactionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = formatId;
        result = 31 * result + Arrays.hashCode(globalTransactionId);
        result = 31 * result + Arrays.hashCode(branchQualifier);
        return result;
    }

    @Override
    public int getFormatId() {
        return formatId;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    /**
     * Write the object to a Base64 string.
     */
    @Override
    public String toString() {
        try {
            return XidImpl.toString(this);
        } catch (IOException e) {
            return formatString();
       }
    }

    public String formatString() {
        return "XidImpl{" +
                "formatId=" + formatId +
                ", globalTransactionId=" + Arrays.toString(globalTransactionId) +
                ", branchQualifier=" + Arrays.toString(branchQualifier) +
                '}';
    }

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();

        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}
