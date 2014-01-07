Standalone JTA 1.2 and Hibernate example.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: JTA, CDI, JPA

What is it?
-----------

This example demonstrates how to use new JTA 1.2 annotations and CDI beans with Hibernate in standalone application.


This example consists of two separate use cases: standalone JTA 1.2 only and standalone JTA 1.2 with Hibernate.
org.jboss.narayana.jta package contains classes which demonstrates usage of JTA only. org.jboss.narayana.jta.jpa package demonstrates both Hibernata and JTA.


System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.


Build and Deploy the Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
2. Execute quickstart:

        mvn clean test