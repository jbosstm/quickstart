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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;

public abstract class AbstractExampleXAResource implements XAResource {
    static String DATA_DIR = "target/";

    private boolean recovered;
    private int timeout = 10;

    public AbstractExampleXAResource(boolean recovered) {
 //       System.out.println(this.getClass().getName() + ": " + " (Constructor) recovered = " + recovered);
        this.recovered = recovered;
    }

    /**
     * 
     * @param xid <description>
     * @param flags <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public void start(Xid xid, int flags) throws XAException {
        System.out.println(this.getClass().getName() + ": " + "start");
    }

    /**
     * 
     * @param xid <description>
     * @param flags <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public void end(Xid xid, int flags) throws XAException {
        System.out.println(this.getClass().getName() + ": " + "end");
    }

    /**
     * 
     * @param xid <description>
     * @return <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public synchronized int prepare(Xid xid) throws XAException {
        System.out.println("******: " + this.getClass().getName() + ": " + "PREPARE " + xid);

        File prepared = new File(DATA_DIR + this.getClass().getName() + ".xid_");
        try {
            prepared.createNewFile();
            final int formatId = xid.getFormatId();
            final byte[] gtrid = xid.getGlobalTransactionId();
            final int gtrid_length = gtrid.length;
            final byte[] bqual = xid.getBranchQualifier();
            final int bqual_length = bqual.length;

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(prepared));
            fos.writeInt(formatId);
            fos.writeInt(gtrid_length);
            fos.write(gtrid, 0, gtrid_length);
            fos.writeInt(bqual_length);
            fos.write(bqual, 0, bqual_length);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new XAException(XAException.XAER_RMERR);
        }
        return XA_OK;
    }

    /**
     * 
     * @param xid <description>
     * @param onePhase <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public synchronized void commit(Xid xid, boolean onePhase) throws XAException {
        System.out.println("******\n" + this.getClass().getName() + ": " + "commit,xid=" + xid + ",onePhase=" + onePhase);

        if (!recovered) {
            Runtime.getRuntime().halt(0);
        }

        File file = new File(DATA_DIR + this.getClass().getName() + ".xid_");
        if (file.exists()) {
            if (!file.delete())
                throw new XAException("Could not removed committed state");
        }
    }

    /**
     * 
     * @param xid <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public synchronized void rollback(Xid xid) throws XAException {
        System.out.println(this.getClass().getName() + ": " + "rollback");

        File prepared = new File(this.getClass().getName() + ".xid_");
        File committed = new File(this.getClass().getName() + ".xid");
        if (prepared.exists()) {
            if (!prepared.renameTo(committed))
                throw new XAException("Could not rename prepared state");
        } else {
            try {
                committed.createNewFile();
            } catch (IOException e) {
                throw new XAException("Could not create committed state");
            }
        }
    }

    /**
     * 
     * @param xid <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public void forget(Xid xid) throws XAException {
        System.out.println(this.getClass().getName() + ": " + "forget");
        // an actual resource would ned to look up the relevant file and remove it
        File file = new File(DATA_DIR + this.getClass().getName() + ".xid_");
    }

    /**
     * 
     * @param flag <description>
     * @return <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public synchronized Xid[] recover(int flag) throws XAException {
//        System.out.println(this.getClass().getName() + ": " + "recover");

        List<Xid> xids = new ArrayList<Xid>();
        File file = new File(DATA_DIR + this.getClass().getName() + ".xid_");

        if (file.exists()) {

            try {
                DataInputStream fis = new DataInputStream(new FileInputStream(file));
                final int formatId = fis.readInt();
                final int gtrid_length = fis.readInt();
                final byte[] gtrid = new byte[gtrid_length];
                fis.read(gtrid, 0, gtrid_length);
                final int bqual_length = fis.readInt();
                final byte[] bqual = new byte[bqual_length];
                fis.read(bqual, 0, bqual_length);
                Xid xid = new Xid() {

                    @Override
                    public byte[] getGlobalTransactionId() {
                        return gtrid;
                    }

                    @Override
                    public int getFormatId() {
                        return formatId;
                    }

                    @Override
                    public byte[] getBranchQualifier() {
                        return bqual;
                    }

                    public String toString() {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("< formatId=");
                        stringBuilder.append(formatId);
                        stringBuilder.append(", gtrid_length=");
                        stringBuilder.append(gtrid_length);
                        stringBuilder.append(", bqual_length=");
                        stringBuilder.append(bqual_length);
                        stringBuilder.append(", tx_uid=");
                        stringBuilder.append(new Uid(gtrid).stringForm());
                        stringBuilder.append(", node_name=");
                        stringBuilder.append(new String(Arrays.copyOfRange(gtrid, Uid.UID_SIZE, gtrid_length)));
                        stringBuilder.append(", branch_uid=");
                        stringBuilder.append(new Uid(bqual));

                        stringBuilder.append(", subordinatenodename=");

                        int offset = Uid.UID_SIZE + 4;
                        int length = (bqual[offset++] << 24) + ((bqual[offset++] & 0xFF) << 16)
                                + ((bqual[offset++] & 0xFF) << 8) + (bqual[offset++] & 0xFF);
                        if (length > 0)
                            stringBuilder.append(new String(Arrays.copyOfRange(bqual, offset, offset + length)));

                        stringBuilder.append(", eis_name=unknown");
                        stringBuilder.append(" >");

                        return stringBuilder.toString();
                    }
                };

                xids.add(xid);

                System.err.println("expect recovery on " + xid);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return xids.toArray(new Xid[xids.size()]); //new Xid[0];
    }

    /**
     * 
     * @param other <description>
     * @return <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public boolean isSameRM(XAResource other) throws XAException {
//        System.out.println(this.getClass().getName() + ": " + "isSameRM");
        return (false);
    }

    /**
     * 
     * @return <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public int getTransactionTimeout() throws XAException {
        System.out.println(this.getClass().getName() + ": " + "getTransactionTimeout");
        return timeout;
    }

    /**
     * 
     * @param seconds <description>
     * @return <description>
     * @exception javax.transaction.xa.XAException <description>
     */
    public boolean setTransactionTimeout(int seconds) throws XAException {
        System.out.println(this.getClass().getName() + ": " + "setTransactionTimeout");
        this.timeout = seconds;
        return true;
    }
}
