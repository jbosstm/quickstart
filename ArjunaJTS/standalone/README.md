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

Using the run script will run using OpenJDK ORB.


### Driving JTS transaction remotely with ORB API

There is also a second example that you must run manually using a bash script.
The example shows how a client can connect to a
remote transaction manager (TM) using a CORBA Name Service for looking up the TM.
The example runs using OpenJDK ORB:

You need to set 2 environment variables to run the example:

`JAVA_HOME`: the location of the JDK (JDK8 is needed for orbd)

`NARAYANA_HOME`: the location of the narayana distribution
 (you can download it from the http://narayana.io/downloads/ while choosing the option 'Naryana Binary')

The script will also set:

`HOST_ADDRESS`: the IP of one of your network interfaces. Do **not** use the loopback connection (ie `localhost` or `127.0.0.1`)
please override the setting in `start.sh` if it is incorrect.

> If you are running on Windows you will need to port
  the script to Windows batch commands.

Running with OpenJDK ORB:

1. start a CORBA name server
```
./start.sh NS
```

2. start a Transaction Recovery Manager
```
./start.sh RM
```

3. start a JTS Transaction Manager
```
./start.sh TM
```

4. start a transactional client program
```
./start.sh CL
```

## Expected output


> The `orbd` command is not bundled with JDK >= 11, see [JEP 320](https://openjdk.java.net/jeps/320) for more information.
> A quick way to get this quickstart running is to reuse the `orbd` bundled binary.
>
> Install JDK 8, locate the `orbd` executable under the `bin` directory and create a symbolic link pointing to it, like so:
> `sudo ln -s ${JDK_8_LOCATION}/bin/orbd /usr/bin/orbd`. Tested with OpenJDK 11.
The script will run the example using the OpenJDK ORB.

Testing against OpenJDK ORB

The run should produce a line indicating success:

```
...
[INFO] BUILD SUCCESS
```
