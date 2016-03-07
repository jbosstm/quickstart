Integration with Narayana and Karaf
==================================================================================================
Author: Amos Feng;
Level: Intermediate;
Technologies: JTA, JTS, OSGI

What is it?
-----------

This example shows how to use the narayana transaction manager and the recovery service in the ogsi container karaf.


System requirements
-------------------

You need to build this project with Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.
You need to build the karaf 4.1.0-SNAPSHOT from the https://github.com/apache/karaf

       git clone https://github.com/apache/karaf
       cd karaf
       mvn clean install
       unzip assemblies/apache-karaf/target/apache-karaf-4.1.0-SNAPSHOT.zip


Build and Run the Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
* Build and run the tests:

        mvn clean install
* Start the karaf

        cd /path/to/apache-karaf-4.1.0-SNAPSHOT
        bin/karaf
* Install the transaction features and quickstart bundle in the karaf

        karaf@root()> repo-add mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features
        karaf@root()> feature:install pax-jdbc-pool-narayana jdbc pax-jdbc-h2 transaction-manager-narayana jndi
        karaf@root()> jdbc:ds-create --driverName H2-pool-xa -dbName test test
        karaf@root()> bundle:install -s mvn:org.jboss.narayana.quickstarts.osgi/osgi-jta-example/5.3.3.Final-SNAPSHOT
* Run the commit example

        karaf@root()> narayana-quickstart:testCommit
        DummyXAResource XA_PREPARE [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:854e:570710ee:133, node_name=1, branch_uid=0:ffff7f000001:854e:570710ee:134, subordinatenodename=null, eis_name=0 >]
        DummyXAResource XA_COMMIT  [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:854e:570710ee:133, node_name=1, branch_uid=0:ffff7f000001:854e:570710ee:134, subordinatenodename=null, eis_name=0 >] with fault NONE
* Run the recovery example with "-f" option and it will crash and halt the karaf

        karaf@root()> narayana-quickstart:testRecovery -f
        testRecovery generate something to recovery
        DummyXAResource XA_PREPARE [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:a4fe:5707105c:59, node_name=1, branch_uid=0:ffff7f000001:a4fe:5707105c:5a, subordinatenodename=null, eis_name=0 >]
        DummyXAResource XA_COMMIT  [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:a4fe:5707105c:59, node_name=1, branch_uid=0:ffff7f000001:a4fe:5707105c:5a, subordinatenodename=null, eis_name=0 >] with fault HALT
* Edit etc/org.jboss.narayana.cfg to add the following

        RecoveryEnvironmentBean.periodicRecoveryPeriod= 10
        RecoveryEnvironmentBean.recoveryBackoffPeriod = 10
* Restart the karaf and wait for the recovery
        You have to run the testRecovery as quickly after restarting the karaf because the recovery manager could commit to the h2 database resource before you run the example

        bin/karaf
        karaf@root()> narayana-quickstart:testRecovery
        current commitRequests of the DummyXAResource is 0
        query the database before recovery
        1 entries in the example
        id = 1, name = commit
        register the DummyXAResourceRecovery
        testRecovery waiting ...
        [Periodic Recovery] DummyXAResourceRecovery Added DummyXAResource: be497c5e-c2c5-4956-8ba7-6921fef6daeb_
        [Periodic Recovery] DummyXAResourceRecovery returning list of DummyXAResources of length: 1
        DummyXAResource XA_COMMIT  [< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:a4fe:5707105c:59, node_name=1, branch_uid=0:ffff7f000001:a4fe:5707105c:5a, subordinatenodename=null, eis_name=0 >] with fault NONE
        testRecovery done
        commitRequests of the DummyXAResource after recovery is 1
        reopen the database
        query the database after recovery
        2 entries in the example
        id = 1, name = commit
        id = 1, name = recovery
        karaf@root()>
