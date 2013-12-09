Standalone JTA 1.2 example.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: JTA, CDI

What is it?
-----------

This example demonstrates how to use new JTA 1.2 annotations and CDI beans in standalone application.

This example consists of four classes:

* One bean class annotated with @Transactional annotation.
* One bean class annotated with @Transactional(Transactional.TxType.MANDATORY) annotation.
* One class which is annotated with @TransactionScoped annotation and injected to both transactional beans.
* Test class which is demonstrating the behaviour of three above classes.

System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.


Build and Deploy the Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
2. Execute quickstart:

        mvn clean test