# Narayana maven quickstart

## Overview

This shows an example of how to include the `org.jboss.narayana.jta:narayana-jta`
 artifact in your own projects


## Usage

```
mvn compile exec:exec
```

or

```
./run.[sh|bat]
```


## Expected output

As well as the maven output you would normally expect, you should also see the following:

```
 com.arjuna.ats.arjuna.recovery.TransactionStatusManager addService
INFO: ARJUNA12163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 51393
23-Jun-2011 15:56:07 com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem <init>
INFO: ARJUNA12337: TransactionStatusManagerItem host: 127.0.0.1 port: 51393
23-Jun-2011 15:56:08 com.arjuna.ats.arjuna.recovery.TransactionStatusManager start
INFO: ARJUNA12170: TransactionStatusManager started on port 51393 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService
TransactionImple < ac, BasicAction: 0:ffff7f000001:b89f:4e035407:2 status: ActionStatus.RUNNING >
null
```

## What just happened

We created a transaction
