Example JBoss Transactions, Iron Jacamar, and Hibernate.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: JTA, JCA, Hibernate

What is it?
-----------

This example demonstrates how to integrate Narayana, IronJacamar, and Hibernate.
The code is based on _Narayana, IronJacamar, and Tomcat_ quickstart.

Embedded IronJacamar container is used in this quickstart. All its dependencies can be found in pom.xml.
Additionally, in order to use XA Datasources, JCA resource adapter is needed (see src/main/resources/jdbc-xa.rar). Although we provide the jdbc-xa.rar file here for testing purposes, customers are advised to download IronJacamar and obtain the latest version in their own applications.

System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.


Build and Deploy the Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
2. Execute quickstart:

        mvn clean test
        
3. You will see the TRACE level logs of the quickstart execution in the output.