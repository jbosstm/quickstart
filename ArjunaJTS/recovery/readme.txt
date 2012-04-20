JBoss, Home of Professional Open Source
Copyright 2012, Red Hat Middleware LLC, and individual contributors
as indicated by the @author tags.
See the copyright.txt in the distribution for a
full listing of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU Lesser General Public License, v. 2.1.
This program is distributed in the hope that it will be useful, but WITHOUT A
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License,
v.2.1 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA  02110-1301, USA.

(C) 2012
@author JBoss Inc.

OVERVIEW
--------

The JBossTS transaction manager supports recovery from failures during the commit phase of a transaction.
This example demonstrate that functionality.

Run the quickstart in two passes. The first run (controled by a command line arg of -crash)
starts a transaction, enlists two XA resources and then commits the transaction. Both resources
prepare but when the first resource is asked to commit it halts the VM thus generating a "recovery record".

In the second run (controled by a command line arg of -recover) the example registers an XAResourceRecovery
instance (whose purpose is explained below) and waits for the recovery system to commit both resources.

USAGE
-----
./run.[sh|bat]

To run an example manually you will need to run it twice, once with a flag to tell the example to
generate a failure followed by a second run with a flag to tell the example to recover the failed transaction.

mvn -e clean compile exec:java -Dexec.mainClass=Test -Dexec.args="-crash"

mvn -e exec:java -Dexec.mainClass=Test -Dexec.args="-recover"

When you see that both resources have recovered press the enter key to end the program.

Remark: If you testing on the master branch of narayana you have the option of switching ORBs from JacORB
over to the orb bundled with the JDK. If you have created a narayana build containing this alternative
orb then you must define the following two properties when running this quickstart:

    -Dcom.sun.CORBA.POA.ORBServerId=1 -Dcom.sun.CORBA.POA.ORBPersistentServerPort=12567

[The link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4505128 provides some details about
why you would need to define these].

EXPECTED OUTPUT
---------------

In the first run you should see output showing both XA resources are prepared:

    ******: ExampleXAResource1: PREPARE < 131072, 29, 36, 0000000000-1-112700100-33879-11148-1250002749, 2929292929292929292928281562929302929-437108-8277-96292929567829292929292929 >
    ExampleXAResource2: end
    ******: ExampleXAResource2: PREPARE < 131072, 29, 36, 0000000000-1-112700100-33879-11148-1250002749, 2929292929292929292928281562929302929-437108-8277-96292929567829292929292929 >

There will also be two files that the XA resources use to remember XIDs and a directory where
the TM stores its transaction logs:

target/ExampleXAResource1.xid_  target/ExampleXAResource2.xid_  target/tx-object-store

The tx-object-store should look similar to following:

target/tx-object-store
└── ShadowNoFileLockStore
    └── defaultStore
        ├── CosTransactions
        │   └── XAResourceRecord
        │       ├── 0_ffff7f000001_de5a_4f9131a1_14
        │       └── 0_ffff7f000001_de5a_4f9131a1_18
        ├── Recovery
        │   ├── FactoryContact
        │   │   └── 0_ffff7f000001_de5a_4f9131a1_e
        │   └── TransactionStatusManager
        │       └── 0_ffff7f000001_de5a_4f9131a1_e
        ├── RecoveryCoordinator
        │   └── 0_ffff52e38d0c_c91_4140398c_0
        └── StateManager
            └── BasicAction
                └── TwoPhaseCoordinator
                    └── ArjunaTransactionImple
                        └── 0_ffff7f000001_de5a_4f9131a1_11

Of particular note is the last entry (0_ffff7f000001_de5a_4f9131a1_11) which represents the prepared
transaction. After the second run the recovery system should resolve this entry.

During the second run the recovery system will print a message saying it is about to recover:

    expect recovery on < formatId=131072, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:de5a:4f9131a1:11, node_name=1, branch_uid=0:ffff7f000001:de5a:4f9131a1:13, subordinatenodename=, eis_name=unknown >
    Apr 20, 2012 10:55:01 AM com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord doRecovery
    INFO: ARJUNA024001: XA recovery committing < 131072, 29, 36, 0000000000-1-112700100-349079-11149-950001749, 2929292929292929292928281562929302929-5119108-8278-66292929467829292929292929 >

Notice that the tx_uid it is recovering is the same one that is in the filesystem (0_ffff7f000001_de5a_4f9131a1_11).

And the example resource prints out the following when it is asked to commit:

    ******
    ExampleXAResource1: commit,xid=< 131072, 29, 36, 0000000000-1-112700100-349079-11149-950001749, 2929292929292929292928281562929302929-5119108-8278-66292929467829292929292929 >,onePhase=false

You will see similar paired ouput lines corresponding to ExampleXAResource2.

NOTE: You may also see, intermittently, an org.omg.CORBA.OBJECT_NOT_EXIST exception trace on the console.
Although recovery has still taken place the warning is not good (there is a JIRA for it and should be fixed
for final). The exception sometimes, though not always, results in the system moving the log record to an
AssumedCompleteTransaction directory (after the next recovery pass).

When you notice both resources have recovered you can end the demonstration by pressing the enter key.
Running the example via the run.[sh|bat] script waits a fixed period (80 seconds) and then finishes.

WHAT JUST HAPPENED?
-------------------
During run 1 (with the -crash argument) the Test program starts a transaction enlists 2 XA resources
(ExampleXAResource1 and ExampleXAResource2), and then commits the transaction. Whichever XA resource
is asked to commit first will halt the VM.

When an XA resource is asked to prepare it is given an Xid to indicate which work should be prepared.
In the example these Xids are stored in the file system (in files with extension .xid_) so they can be
retrieved by the XA resources when asked to recover.

During run 2 (with the -recover argument) the Test program ensures that recovery is configured and then
waits for the user to press enter (or waits for a timer if started with a -auto argument) to end the program. 

The ExampleXAResourceRecovery helper is registered with the recovery system (via the jtaPropertyManager).
XAResourceRecovery instances are one of the techniques the TM uses to reconnect to resources that were in
use prior to a crash in order to resolve outstanding transactions. The XAResourceRecovery instance gives
the recovery system new XA resources via a method called getXAResource() which recovery system can then
use to perform the actual recovery.

The example XAResourceRecovery instance in this quickstart returns XAResources instances that do not
halt the VM when asked to commit (in contrast to the ones enlisted in crash phase of this quickstart).
Instead they print out a message to the console indicating that they have been recovered. These example
resources also know how to find Xids for failed branches when asked to recover via their recover() method.
Recall that during prepare the example resources store the Xid for the work being prepared in files with
extension .xid_ so the recover() method simply looks for the appropriate file and converts its contents
into an Xid and returns it to the recovery system.

The recovery system then replay the commit phase of the transaction at the appropriate time.
When the example XA resources are asked to commit they remove the corresponding .xid_ files. Thus if these
files are missing after running the quickstart then you can be sure that recovery has been successful.
