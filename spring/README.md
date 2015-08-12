Integration with Narayana and Spring
==================================================================================================
Author: Amos Feng;
Level: Intermediate;
Technologies: JTA, JTS, Spring 

What is it?
-----------

The jta example shows how to config the narayana transaction manager with spring annotation.



System requirements
-------------------

All you need to build this project is Java 7.0 (Java SDK 1.7) or better and Maven 3.0 or better.


Build and Run the Spring JTA Quickstart
-------------------------------

1. Open a command line and navigate to the root directory of this quickstart.
* Build and run the tests:

        mvn clean install
* Run the commit example

        java -jar jta/target/spring-jta.jar
* Run the recovery example

        java -jar jta/target/spring-jta.jar -f
        java -jar jta/target/spring-jta.jar -r
* Check the database after recovery

        java -jar jta/target/spring-jta.jar -c
