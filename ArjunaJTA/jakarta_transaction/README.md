## Jakarta transaction quickstart

## Overview

This example shows how to obtain a Jakarta EE conforming transaction and how to invoke various methods
of the `jakarta.transaction.Transaction` interface such as starting and ending transactions, examining
transacton status, timeouts etc.

## Usage

```
mvn compile exec:exec
```

or

```
./run.[sh|bat]
```

## Expected output

```
[INFO] BUILD SUCCESS

otherwise you can examine what went wrong by enabling stack traces with the -e flat:
	mvn compile exec:exec -e
```

## What just happened

The example looks up an instance of the Java EE `UserTransaction` using and calls its various methods.
If anything goes wrong an exception is generated. The example shows how to get
the instance of the `TransactionManager` interface.
