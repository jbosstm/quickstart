# Trailmap JTS distributed transaction processing

This quickstart shows how to use ORB remote calls
wrapped with the transaction processing.

**README needs to be fixed**

#Run the simple sample

`mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=com.arjuna.demo.simple.HelloServer &`

`mvn clean compile exec:java  -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=com.arjuna.demo.simple.HelloClient`

#Output
```
The Hello server is now ready... 

Creating a transaction !
Jul 06, 2022 10:53:44 AM com.arjuna.ats.arjuna.recovery.TransactionStatusManager start
INFO: ARJUNA012170: TransactionStatusManager started on port 46133 and host 127.0.0.1 with service > com.arjuna.ats.arjuna.recovery.ActionStatusService
Call the Hello Server !
Hello - called within a scope of a transaction
Commit transaction
Done 
```

# Deprecated instructions

To build the sources files you should follow instructions given below:

- Ensure you have the Ant build system installed. Ant is a Java build
tool, similar to make.
  It is available for free from http://ant.apache.org/
  The sample application requires version 1.5.1 or later.

- The PATH and CLASSPATH environment variables need to be set
appropriately to use JBoss Transaction Service.

  To make this easier, we provide a shell script setup-env.sh (and for
  Windows a batch file setup-env.bat) which you  can either source, or
  use to input into your own environment. These scripts are located in
  in the directory `<jbossts_install_root>/bin/`

  JNDI is recommended way to use XADataSource because it isolates the application from the
  different jdbc implementations. The JNDI implementation, that the jdbcbank sample uses is fscontext.jar,
  which can be download from http://java.sun.com/products/jndi/downloads/index.html

Important Note:

 From a command prompt,  go (or 'cd') to  the directory containing the
 build.xml file (`<jbossjts_install_root>/trailmap`) and type 'ant'.

 Add   the  generated file  named   jbossts-demo.jar and located under
 `<jbossjts_install_root>/trailmap/lib`  in    you  `CLASSPATH` environment
 variable.

When running the local JTS transactions part of the trailmap, you will need to start
the recovery manager: java com.arjuna.ats.arjuna.recovery.RecoveryManager -test

 For each sample, refer to the appropriate trail page.

 Database Note:

 The out-of-the-box configuration assumes an Oracle database. If you want
 to use MSSQLServer, then you need to do the following:

	1. install SQL Server JDBC XA procedures http://edocs.bea.com/wls/docs81/jdbc_drivers/mssqlserver.html#1075232
	2. Start up the Microsoft Distributed Transaction Coordinator (DTC) http://msdn2.microsoft.com/en-US/library/ms378931(SQL.90).aspx

If to shift from using Oracle as in jdbcbank example to Microsoft SQLServer 2000, the
initialization code for XADataSource should be replaced. The jdbc driver which may be used is available from:
http://www.microsoft.com/downloads/details.aspx?familyid=07287B11-0502-461A-B138-2AA54BFDC03A&displaylang=en

```java
  SQLServerDataSource ds = new com.microsoft.jdbcx.sqlserver.SQLServerDataSource();
  ds.setDescription("MSSQLServer2k DataSource");
  ds.setServerName(host);
  ds.setPortNumber(1433);
  ds.setDatabaseName(dbName);
  ds.setSelectMethod("cursor"); //It's a Must emphasized in Driver's User Manual
```

to replace

```java
  Class oracleXADataSource = Class.forName("oracle.jdbc.xa.client.OracleXADataSource");
  DataSource ds = (DataSource) oracleXADataSource.newInstance();
  Method setUrlMethod = oracleXADataSource.getMethod("setURL", new Class[]{String.class});
  setUrlMethod.invoke(ds, new Object[]{new String("jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName)});
```
