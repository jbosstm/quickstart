Example JBoss Transactions, Iron Jacamar, and Tomcat.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: JTA, JCA, Tomcat

What is it?
-----------

This example demonstrates how to integrate JBossTS, IronJacamar, and Tomcat.
The code is based on _JTA_ and _cmt_ quickstarts.

Embedded IronJacamar container is used in this quickstart. All its dependencies can be found in pom.xml.
Additionally, in order to use XA Datasources, JCA resource adapter is needed (see src/main/resources/jdbc-xa.rar). Although we provide the jdbc-xa.rar file here for testing purposes, customers are advised to download IronJacamar and obtain the latest version in their own applications.

System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better, Maven 3.0 or better, and Tomcat 7.0 or better.


Configure PostgreSQL
--------------------

This quickstart requires the PostgreSQL database. Instructions to install and configure PostgreSQL can be found here: [Install and Configure the PostgreSQL Database](http://www.jboss.org/jdf/quickstarts/jboss-as-quickstart/#postgresql)

_Note_: For the purpose of this quickstart, replace the word QUICKSTART_DATABASENAME with `jca-and-tomcat-quickstart-database` in the PostgreSQL instructions.

Be sure to start the PostgreSQL database. Unless you have set up the database to automatically start as a service, you must repeat the instructions "Start the database server" for your operating system every time you reboot your machine.


Build and Deploy the Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
2. Build quickstart archive:

        mvn clean package
        
3. Undeploy previous deployment of the quickstart if such exists. See [Undeploy the Archive](#undeploy-the-archive)

4. Copy quickstart archive to the Tomcat deployments directory:
 
        cp target/jca-and-tomcat.war $TOMCAT_HOME/webapps

5. Start Tomcat:

        $TOMCAT_HOME/bin/catalina.sh run


Access the application 
----------------------

To access the application type the following into a browser: <http://localhost:8080/jca-and-tomcat/>

You will be presented with a simple form for adding customers to a database.

When you enter a name and click "Add" that customer, you will see the INFO level logging of the DummyXAResource in the Tomcat console. In the browser you will see the list of customer added so far and number of successfully commited transactions since the server was started.


Testing crash recovery
----------------------

In order to see crash recovery in action please download and extract [Byteman](http://www.jboss.org/byteman/downloads) and follow these steps:

1. Deploy application as described above.
2. Export BYTEMAN_HOME:

        export BYTEMAN_HOME={Path to the Byteman directory}
        
3. Deploy byteman rule:

        $BYTEMAN_HOME/bin/bminstall.sh -b -Dorg.jboss.byteman.transform.all -Dorg.jboss.byteman.verbose org.apache.catalina.startup.Bootstrap
        $BYTEMAN_HOME/bin/bmsubmit.sh -l {Path to the quickstart}/src/test/resources/fail2pc.btm

4. Add customer as explained above.
5. Restart Tomcat:

        $TOMCAT_HOME/bin/catalina.sh run
        
Steps 3 will register Byteman rule which will kill Tomcat server after you will try to add customer in step 4. It will lead to the uncompleted two phase commit protocol.
After you will restart Tomcat in step 5, recovery system will kick in and finish the transaction. You will see the INFO level logging of the DummyXAResource in Tomcat console. New customer entry should be available in around 40 seconds after restart i.e. after second recovery cycle. You can find customers list here: <http://localhost:8080/jca-and-tomcat/customers.xhtml>
  
NOTE: you will see the warning messages similar to [1] and [2] after you restart Tomcat. It happens because recovery system starts before Postgres data source is deployed. Therefore, recovery system cannot resolve Postgres XAResource the first time. Recovery completes successfully in the second iteration, after the data source is deployed.

        [1] [Periodic Recovery] WARN com.arjuna.ats.jta - ARJUNA016037: Could not find new XAResource to use for recovering non-serializable XAResource XAResourceRecord < resource:null, txid:< formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:94bc:51b5ab83:10, node_name=1, branch_uid=0:ffff7f000001:94bc:51b5ab83:15, subordinatenodename=null, eis_name=0 >, heuristic: TwoPhaseOutcome.FINISH_OK com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord@253de8d >
        [2] [Periodic Recovery] WARN com.arjuna.ats.jta - ARJUNA016038: No XAResource to recover < formatId=131077, gtrid_length=29, bqual_length=36, tx_uid=0:ffff7f000001:94bc:51b5ab83:10, node_name=1, branch_uid=0:ffff7f000001:94bc:51b5ab83:15, subordinatenodename=null, eis_name=0 >

Undeploy the Archive
--------------------

1. Stop Tomcat.
2. Delete jca-and-tomcat directory and jca-and-tomcat.war archive from the $TOMCAT_HOME/webapps directory.
