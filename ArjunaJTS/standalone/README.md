
OVERVIEW
--------
An example of how to start and end JTS transactions. A JTS transaction requires an ORB.

USAGE
-----
mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.TransactionExample

or
./run.[sh|bat]

Using the run script will run the example twice, once with JacOrb and then again but using JdkOrb.

There is also a second example that you must run manually using a bash script. If you are running on windows
you will need to port the script to Windows batch commands. The example shows how a client can connect to a
remote transaction manager (TM) using a CORBA Name Service for looking up the TM. The example runs
using either JacOrb or JdkOrb:

You need to set 2 environment variables to run the example:

JAVA_HOME: the location of the JDK
NARAYANA_HOME: the location of the narayana distribution

The script will also set:
HOST_ADDRESS: the IP of one of your network interfaces. Do not use the loopback connection (ie localhost or 127.0.0.1)
please override the setting in start.sh if it is incorrect.

Running with JacOrb:

1) start a CORBA name server
./start.sh jacorb NS

2) start a Transaction Recovery Manager
./start.sh jacorb RM

3) start a JTS Transaction Manager
./start.sh jacorb TM

4) start a transactional client program
./start.sh jacorb CL

Running with JdkORB:

as above but replace jacorb with jdkorb

EXPECTED OUTPUT
---------------

The run script will the example twice, the first run uses JacORB for the CORBA ORB followed by a second run using
the ORB bundled with the standard JDK.

Both runs should produce a line indicating success:

Testing against JacOrb
...
[INFO] BUILD SUCCESS

Testing against JdkOrb
...
[INFO] BUILD SUCCESS


