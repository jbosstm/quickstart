package org.jboss.narayana;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.ByteArrayOutputStream;

import java.io.PrintWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulate a variety of faults during the various phases of the XA protocol
 */
public class DummyXAResource implements XAResource, Serializable // ,Synchronization
{
    private final static String XID_FNAME = "xids.txt";
    private final static String XAR_LOG_FNAME = "xar.log";
    private static final Map<String, XAException> xaCodeMap = new HashMap<String, XAException>();
    private static XidStore xidStore = new XidStore(XID_FNAME);
    private static XidStore traceLog = new XidStore(XAR_LOG_FNAME);

    private ASFailureType _xaFailureType = ASFailureType.NONE;
    private ASFailureMode _xaFailureMode = ASFailureMode.NONE;
    private int _suspend;
    private XAException _xaException;
    private int txTimeout = 10;
    private transient boolean _isPrepared = false; // transient so it doesn't get persisted in the tx store
    private String _xid = null;

    static
    {
        init();
    }

    public DummyXAResource()
    {
        this(new ASFailureSpec("default", ASFailureMode.NONE, "", ASFailureType.NONE));
    }

    public DummyXAResource(ASFailureSpec spec)
    {
        if (spec == null)
            throw new IllegalArgumentException("Invalid XA resource failure injection specification");

        setFailureMode(spec.getMode(), spec.getModeArg());
        setFailureType(spec.getType());
    }

    public void applySpec(Xid xid, String message) throws XAException
    {
        applySpec(xid, message, _isPrepared);
    }

    public void applySpec(Xid xid, String message, boolean prepared) throws XAException
    {
        if (_xaFailureType.equals(ASFailureType.NONE) || _xaFailureMode.equals(ASFailureMode.NONE) || !prepared)
        {
            debug(xid, message + (_isPrepared ? " ... " : " recovery"));
            return; // NB if !_isPrepared then we must have been called from the recovery subsystem
        }

        debug(xid, "Applying fault injection with active branches");
        if (_xaException != null)
        {
            debug(xid, message + " ... xa error: " + _xaException.getMessage());
            throw _xaException;
        }
        else if (_xaFailureMode.equals(ASFailureMode.HALT))
        {
            debug(xid, message + " ... halting");
            Runtime.getRuntime().halt(1);
        }
        else if (_xaFailureMode.equals(ASFailureMode.EXIT))
        {
            debug(xid, message + " ... exiting");
            System.exit(1);
        }
        else if (_xaFailureMode.equals(ASFailureMode.SUSPEND))
        {
            debug(xid, message + " ... suspending for " + _suspend);
            suspend(_suspend);
            debug(xid, message + " ... resuming");
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("XAResourceWrapperImpl@").append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" pad=").append("false");
        sb.append(" overrideRmValue=").append("false");
        sb.append(" productName=").append("Dummy Product");
        sb.append(" productVersion=").append("0.0.0");
        sb.append(" jndiName=").append("java:/dummyProd");
        sb.append("]");

        return sb.toString();
    }

    private void suspend(int msecs)
    {
        try
        {
            Thread.sleep(msecs);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void setFailureMode(ASFailureMode mode, String ... args) throws IllegalArgumentException
    {
        _xaFailureMode = mode;

        if (args != null && args.length != 0)
        {
            if (_xaFailureMode.equals(ASFailureMode.SUSPEND))
            {
                _suspend = Integer.parseInt(args[0]);
            }
            else if (_xaFailureMode.equals(ASFailureMode.XAEXCEPTION))
            {
                _xaException = xaCodeMap.get(args[0]);

                if (_xaException == null)
                    _xaException = new XAException(XAException.XAER_RMFAIL);
            }
        }
    }

    public void setFailureType(ASFailureType type)
    {
        _xaFailureType = type;
    }

    public ASFailureType getFailureType()
    {
        return _xaFailureType;
    }

    // Synchronizatons

    public void beforeCompletion()
    {
        if (_xaFailureType.equals(ASFailureType.SYNCH_BEFORE))
            try
            {
                applySpec(null, "Before completion");
            }
            catch (XAException e)
            {
                throw new RuntimeException(e);
            }
    }

    public void afterCompletion(int i)
    {

        if (_xaFailureType.equals(ASFailureType.SYNCH_AFTER))
            try
            {
                applySpec(null, "After completion");
            }
            catch (XAException e)
            {
                throw new RuntimeException(e);
            }
    }

    private void debug(Xid xid, String message) {
        debug(xid, false, message);
    }

    private void debug(Xid xid, boolean stackTrace, String message) {
        String xidStr = xid != null ? new XidImpl(xid).formatString() : "";
        System.out.printf("DummyXAResource: %s%n", message);

        if (stackTrace)
            System.out.println(getStackTrace(null));

        try {
            traceLog.write(s -> s, String.format(": [%s] %s %s",  Thread.currentThread().getName(), xidStr, message));
        } catch (Exception ignore) {
        }
    }

    // XA Interface implementation

    public void commit(Xid xid, boolean b) throws XAException {
        debug(xid, "commit");

        if (_xaFailureType.equals(ASFailureType.XARES_COMMIT))
            applySpec(xid, "xa commit");

        _isPrepared = false;

        try {
            xidStore.remove(xid);
        } catch (Exception ignore) {
        }

    }

    public void rollback(Xid xid) throws XAException
    {
        debug(xid, true, "rollback");

        if (_xaFailureType.equals(ASFailureType.XARES_ROLLBACK))
            applySpec(xid, "xa rollback");

        _isPrepared = false;
        try {
            xidStore.remove(xid);
        } catch (Exception ignore) {
        }
    }

    public void end(Xid xid, int i) throws XAException
    {
        debug(xid, "end");

        if (_xaFailureType.equals(ASFailureType.XARES_END))
            applySpec(xid, "xa end");
    }

    public void forget(Xid xid) throws XAException
    {
        debug(xid, "forget");

        if (_xaFailureType.equals(ASFailureType.XARES_FORGET))
            applySpec(xid, "xa forget");

        _isPrepared = false;

        try {
            xidStore.remove(xid);
        } catch (Exception ignore) {
        }
    }

    public int getTransactionTimeout() throws XAException
    {
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        return xaResource instanceof DummyXAResource;  // the same resource is used for all recovery xids
    }

    public int prepare(Xid xid) throws XAException
    {
        debug(xid, "prepare");

        _isPrepared = true;

        if (_xaFailureType.equals(ASFailureType.XARES_PREPARE))
            applySpec(xid, "xa prepare");

        try {
            xidStore.write(xid);
        } catch (Exception ignore) {
        }

        return XA_OK;
    }

    public Xid[] recover(int i) throws XAException
    {
        if (_xaFailureType.equals(ASFailureType.XARES_RECOVER))
            applySpec(null, "xa recover");

        try {
            List<Xid> xids = xidStore.read(new ArrayList<>());


            return xids.toArray(new Xid[xids.size()]);
        } catch (Exception ignore) {
            return new Xid[0];
        }
    }

    public boolean setTransactionTimeout(int txTimeout) throws XAException
    {
        this.txTimeout = txTimeout;

        return true;    // set was successfull
    }

    public void start(Xid xid, int i) throws XAException
    {
        debug(xid, "start");

        if (_xaFailureType.equals(ASFailureType.XARES_START))
            applySpec(xid, "xa start");
    }

    public String getEISProductName() { return "Test XAResouce";}

    public String getEISProductVersion() { return "v666.0";}

    private String getStackTrace(Exception e) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bytes, true);

        if (e == null) {
            Arrays.stream(Thread.currentThread().getStackTrace()).forEach(writer::println);
        } else {
            Throwable cause = e.getCause();

            e.printStackTrace(writer);

            while (cause != null) {
                writer.write("Caused By:");
                writer.println();
                cause.printStackTrace(writer);
                cause = cause.getCause();
            }
        }

        return bytes.toString();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private static void init()
    {
        xaCodeMap.put("XA_HEURCOM", new XAException(XAException.XA_HEURCOM));
        xaCodeMap.put("XA_HEURHAZ", new XAException(XAException.XA_HEURHAZ));
        xaCodeMap.put("XA_HEURMIX", new XAException(XAException.XA_HEURMIX));
        xaCodeMap.put("XA_HEURRB", new XAException(XAException.XA_HEURRB));
        xaCodeMap.put("XA_NOMIGRATE", new XAException(XAException.XA_NOMIGRATE));
        xaCodeMap.put("XA_RBBASE", new XAException(XAException.XA_RBBASE));
        xaCodeMap.put("XA_RBCOMMFAIL", new XAException(XAException.XA_RBCOMMFAIL));
        xaCodeMap.put("XA_RBDEADLOCK", new XAException(XAException.XA_RBDEADLOCK));
        xaCodeMap.put("XA_RBEND", new XAException(XAException.XA_RBEND));
        xaCodeMap.put("XA_RBINTEGRITY", new XAException(XAException.XA_RBINTEGRITY));
        xaCodeMap.put("XA_RBOTHER", new XAException(XAException.XA_RBOTHER));
        xaCodeMap.put("XA_RBPROTO", new XAException(XAException.XA_RBPROTO));
        xaCodeMap.put("XA_RBROLLBACK", new XAException(XAException.XA_RBROLLBACK));
        xaCodeMap.put("XA_RBTIMEOUT", new XAException(XAException.XA_RBTIMEOUT));
        xaCodeMap.put("XA_RBTRANSIENT", new XAException(XAException.XA_RBTRANSIENT));
        xaCodeMap.put("XA_RDONLY", new XAException(XAException.XA_RDONLY));
        xaCodeMap.put("XA_RETRY", new XAException(XAException.XA_RETRY));
        xaCodeMap.put("XAER_ASYNC", new XAException(XAException.XAER_ASYNC));
        xaCodeMap.put("XAER_DUPID", new XAException(XAException.XAER_DUPID));
        xaCodeMap.put("XAER_INVAL", new XAException(XAException.XAER_INVAL));
        xaCodeMap.put("XAER_NOTA", new XAException(XAException.XAER_NOTA));
        xaCodeMap.put("XAER_OUTSIDE", new XAException(XAException.XAER_OUTSIDE));
        xaCodeMap.put("XAER_PROTO", new XAException(XAException.XAER_PROTO));
        xaCodeMap.put("XAER_RMERR", new XAException(XAException.XAER_RMERR));
        xaCodeMap.put("XAER_RMFAIL ", new XAException(XAException.XAER_RMFAIL));
    }

    public boolean isXAResource()
    {
        return _xaFailureType.isXA() || _xaFailureType.equals(ASFailureType.NONE);
    }

    public boolean isSynchronization()
    {
        return _xaFailureType.isSynchronization();
    }

    public boolean isPreCommit()
    {
        return _xaFailureType.isPreCommit();
    }

    public boolean expectException()
    {
        return _xaFailureMode.equals(ASFailureMode.XAEXCEPTION);
    }
}