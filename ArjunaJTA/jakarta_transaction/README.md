<!--
JBoss, Home of Professional Open Source
Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

(C) 2011
@author JBoss Inc.
-->

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
