# Narayana JTS standalone quickstart

## Overview

An example of how to start and end JTS transactions. A JTS transaction requires an ORB.

## Usage

### Running Narayana JTS transaction

```
mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample
```

or

```
mvn clean compile
./run.[sh|bat]
```

Using the run script will run the example twice, once with JacOrb and then again but using JdkOrb.


### Driving JTS transaction remotely with ORB API

There is also a second example that you must run manually using a bash script.
The example shows how a client can connect to a
remote transaction manager (TM) using a CORBA Name Service for looking up the TM.
The example runs using either JacOrb or JdkOrb:

You need to set 2 environment variables to run the example:

`JAVA_HOME`: the location of the JDK
`NARAYANA_HOME`: the location of the narayana distribution
 (you can download it from the http://narayana.io/downloads/ while choosing the option 'Naryana Binary')

The script will also set:

`HOST_ADDRESS`: the IP of one of your network interfaces. Do **not** use the loopback connection (ie `localhost` or `127.0.0.1`)
please override the setting in `start.sh` if it is incorrect.

> If you are running on Windows you will need to port
  the script to Windows batch commands.

Running with JacOrb:

1. start a CORBA name server
```
./start.sh jacorb NS
```

2. start a Transaction Recovery Manager
```
./start.sh jacorb RM
```

3. start a JTS Transaction Manager
```
./start.sh jacorb TM
```

4. start a transactional client program
```
./start.sh jacorb CL
```

Running with JdkORB:

as above but replace JacORB with JdkORB


## Expected output

The run script will the example twice, the first run uses JacORB for the CORBA ORB followed by a second run using
the ORB bundled with the standard JDK.

Both runs should produce a line indicating success:

Testing against JacORB

```
...
[INFO] BUILD SUCCESS
```

Testing against JdkORB

```
...
[INFO] BUILD SUCCESS
```
