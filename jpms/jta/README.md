# Modular application that uses transactions and recovery

## Overview

The quickstart shows how to use narayana with the Java Platform Module System (JPMS) which was specified by [JEP 261](https://openjdk.org/jeps/261).

At the time of writing narayana has not been modularised. However, the module system supports [incremental modularization](https://dev.java/learn/modules/automatic-module/) allowing plain JARs on the module path where they turn into "automatic modules" thereby permitting modular applications, such as this quickstart, to be written before its dependant JARs have been modularized.

The application functionality is split over four modules (refer to the *module-info.java* files in each module to study what they require from and what they export to other modules). The module names follow the JEP convention of using reverse Internet domain-names and a module's name should correspond to the name of its principal exported API package.

## Structure of the demo module

### io.narayana.txuser

Provides behaviour for starting and ending transactions and for enlisting resources with transactions.

### io.narayana.recovery

Exposes an API for starting and stopping and suspending and resuming the transaction recovery system and for requesting recovery scans and adding recovery helpers (external entities that instantiate and supply their own XAResources to the recovery system for use in recovering failed resources).

### io.narayana.config

Provides a configuartion object for configuring the transaction and recovery modules.

### io.narayana.demo

A module that uses the other three modules. It performs the following steps:

1. Runs the main method of class Demonstration, supplied by the *io.narayana.demo* module, which instantiates and starts the recovery system using the *io.narayana.recovery* module. The recovery system is supplied with configuration by creating an instance of *io.narayana.config.Config* which is defined by the *io.narayana.config* module.
2. Creates two resources, the second of which is configured to fail the initial commit attempt. Then a recovery helper for the failing resource is registered with the recovery module.
3. Starts a transaction and enlists two resources with it, after which it suspends the recovery module. Then the transaction module is asked to finish the transaction which will initially fail because the second resource is configured to fail the initial commit request. To allow the recovery system to finish this failed resource, the recovery system is firt resumed (this was why recovery started suspended otherwise it would have been immediately recovered) and a scan of the transaction logs is requested (`recovery.scan();`). After the scan the demo waits for the resource to notify that it has been successfuly asked to commit (see method `testFinish`).
4. The recovery module includes a method to report pending logs and this is used before, during and after recovery to verify the presence and absence of logs at the appropriate times.

### Running the example

After building the quickstart:

```
cd demo
sh run.sh
```

or just run the tests during the maven build (the pom runs both the demo and the module tests):

```
mvn clean install
```

### Expected output

```
$ sh run.sh
Running JPMS example
[INFO ] 2025-09-06 18:57:21.185 [main] arjuna - ARJUNA012170: TransactionStatusManager started on port 41191 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService
[INFO ] 2025-09-06 18:57:21.194 [main] SimpleResource - SimpleResource prepare called: < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:a, subordinatenodename=null, eis_name=0 >
[INFO ] 2025-09-06 18:57:21.195 [main] SimpleResourceXA_RETRY - SimpleResourceXA_RETRY prepare called: < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:d, subordinatenodename=null, eis_name=0 >
[INFO ] 2025-09-06 18:57:21.196 [main] SimpleResource - SimpleResource commit called: < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:a, subordinatenodename=null, eis_name=0 >
[INFO ] 2025-09-06 18:57:21.196 [main] SimpleResourceXA_RETRY - SimpleResourceXA_RETRY commit called: < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:d, subordinatenodename=null, eis_name=0 >
[WARN ] 2025-09-06 18:57:21.196 [main] SimpleResourceXA_RETRY - Returning XA_RETRY first time
[WARN ] 2025-09-06 18:57:21.197 [main] jta - ARJUNA016036: commit on < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:d, subordinatenodename=null, eis_name=0 > (io.narayana.demo.SimpleResourceXA_RETRY@8fe2ae0) failed with exception $XAException.XA_RETRY
javax.transaction.xa.XAException: null
	at io.narayana.demo.SimpleResourceXA_RETRY.commit(SimpleResourceXA_RETRY.java:32) ~[demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord.topLevelCommit(XAResourceRecord.java:429) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.coordinator.BasicAction.doCommit(BasicAction.java:3031) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.coordinator.BasicAction.doCommit(BasicAction.java:2947) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.coordinator.BasicAction.phase2Commit(BasicAction.java:1942) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.coordinator.BasicAction.End(BasicAction.java:1574) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator.end(TwoPhaseCoordinator.java:71) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.arjuna.AtomicAction.commit(AtomicAction.java:135) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple.commitAndDisassociate(TransactionImple.java:1315) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.arjuna.ats.internal.jta.transaction.arjunacore.BaseTransaction.commit(BaseTransaction.java:104) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at io.narayana.txuser.TxUser.endTransaction(TxUser.java:50) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at io.narayana.demo.Demonstrator.test2(Demonstrator.java:89) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
	at io.narayana.demo.Demonstrator.main(Demonstrator.java:41) [demo-7.2.3.Final-SNAPSHOT-jar-with-dependencies.jar:?]
[INFO ] 2025-09-06 18:57:21.203 [main] Demonstrator - before scan recovered: false
[INFO ] 2025-09-06 18:57:22.211 [main] SimpleResourceXA_RETRY - SimpleResourceXA_RETRY commit called: < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffffc0a80176:9ac1:68bc7601:7, node_name=1, branch_uid=0:ffffc0a80176:9ac1:68bc7601:d, subordinatenodename=null, eis_name=0 >
[INFO ] 2025-09-06 18:57:22.211 [main] Demonstrator - recovered: true
```
### What just happened

This is the output corresponding to the [4 steps](#demo) described earlier:

The shell script prints "Running JPMS example".
The transaction manager (TM) starts.
The TM calls prepare on the two resources.
The TM successfully calls commit on the SimpleResource.
When the TM tries to commit the second resource the message "SimpleResourceXA_RETRY - Returning XA_RETRY first time" is logged and the resource throws an XAException.
The TM catches the exception and logs the stacktrace.
The demo application logs the scan attempt and reports the progress of the commit on the second resource.

