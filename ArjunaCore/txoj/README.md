# How to use Transactional Object for Java

## Overview

This shows an example of how to introduce TXOJ to your project.

TXOJ are described in the ArjunaCore developers guide
(see http://narayana.io//docs/project/index.html)

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
9-Jun-2011 17:24:51 com.arjuna.ats.arjuna.recovery.TransactionStatusManager addService
INFO: ARJUNA12163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 44448
29-Jun-2011 17:24:51 com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem <init>
INFO: ARJUNA12337: TransactionStatusManagerItem host: 127.0.0.1 port: 44448
29-Jun-2011 17:24:51 com.arjuna.ats.arjuna.recovery.TransactionStatusManager start
INFO: ARJUNA12170: TransactionStatusManager started on port 44448 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService
Created persistent object 0:ffff7f000001:ac45:4e0b51d3:0
Atomic object operated as expected
```

## What just happened

We created a transactional object and modified its state in an **ACI** property
transaction (Atomic, Consistent and Isolated, but not Durable)
