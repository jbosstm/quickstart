/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class DummyXAResource implements XAResource {
    private static int commitRequests = 0;
    protected int timeout = 0;
    private transient faultType fault = faultType.NONE;
    private Xid xid;
    private File file;

    public DummyXAResource() {
        this(faultType.NONE);
    }

    public DummyXAResource(faultType fault) {
        this.fault = fault;
    }

    /**
     * This construct is used for the recovery manager to create a new XA Resource
     *
     * @param file a file used to restore the xid
     */
    public DummyXAResource(File file) throws IOException {
        this.file = file;
        DataInputStream fis = new DataInputStream(new FileInputStream(file));
        final int formatId = fis.readInt();
        final int gtrid_length = fis.readInt();
        final byte[] gtrid = new byte[gtrid_length];
        fis.read(gtrid, 0, gtrid_length);
        final int bqual_length = fis.readInt();
        final byte[] bqual = new byte[bqual_length];
        fis.read(bqual, 0, bqual_length);

        this.xid = new Xid() {
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
        };
        fis.close();
    }

    public static int getCommitRequests() {
        return commitRequests;
    }

    public void setFault(faultType fault) {
        this.fault = fault;
    }

    public void commit(final Xid xid, final boolean arg1) throws XAException {
        System.out.println("DummyXAResource XA_COMMIT  [" + xid + "] with fault " + fault);
        commitRequests += 1;

        if (fault != null) {
            if (fault.equals(faultType.HALT)) {
                Runtime.getRuntime().halt(1);
            }
        }

        if (file != null) {
            if (!file.delete()) {
                throw new XAException(XAException.XA_RETRY);
            }
        }
        this.xid = null;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        if (xares instanceof DummyXAResource) {
            DummyXAResource other = (DummyXAResource) xares;
            if ((this.xid != null && other.xid != null)) {
                if (this.xid.getFormatId() == other.xid.getFormatId()) {
                    if (Arrays.equals(this.xid.getGlobalTransactionId(), other.xid.getGlobalTransactionId())) {
                        if (Arrays.equals(this.xid.getBranchQualifier(), other.xid.getBranchQualifier())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public int prepare(final Xid xid) throws XAException {
        System.out.println("DummyXAResource XA_PREPARE [" + xid + "]");

        File dir = new File("DummyXAResource/");
        dir.mkdirs();
        file = new File(dir, UUID.randomUUID() + "_");
        try {
            file.createNewFile();
            final int formatId = xid.getFormatId();
            final byte[] gtrid = xid.getGlobalTransactionId();
            final int gtrid_length = gtrid.length;
            final byte[] bqual = xid.getBranchQualifier();
            final int bqual_length = bqual.length;

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
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

        if (fault.equals(faultType.HEURISTIC)) {
            throw new XAException(XAException.XA_HEURCOM);
        }

        return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException {
        return new Xid[]{xid};
    }

    public void rollback(final Xid xid) throws XAException {
        System.out.println("DummyXAResource XA_ROLLBACK[" + xid + "]");

        if (file != null) {
            if (!file.delete()) {
                throw new XAException(XAException.XA_RETRY);
            }
        }
        this.xid = null;

    }

    public void start(Xid xid, int flags) throws XAException {
    }

    public void end(Xid xid, int flags) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return (timeout);
    }

    public boolean setTransactionTimeout(final int seconds) throws XAException {
        timeout = seconds;
        return (true);
    }

    public enum faultType {HALT, HEURISTIC, NONE}
}
