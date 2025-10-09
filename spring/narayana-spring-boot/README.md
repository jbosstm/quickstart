Narayana Spring Boot example
============================
Author: Gytis Trikleris
Level: Intermediate
Technologies: JTA, JMS, JPA, Spring Boot
Summary: Demonstration of Narayana integration with Spring Boot


What is it?
-----------

This quickstart demonstrates Narayana integration with Spring Boot.

It uses JMS and JPA to demonstrate transactional behaviour. Also, there is a system crash and recovery demonstration available. 


Requirements
------------

All you need to build this project is Java 8.0 (Java SDK 1.8) or later and Maven 3.3.3 or later.


Usage
-----

In your terminal navigate to the quickstart directory and execute one of the following scenarios:

* Commit and rollback demonstration

    mvn clean spring-boot:run -Dspring-boot.run.arguments="commit Test-Value"
    
    mvn clean spring-boot:run -Dspring-boot.run.arguments="rollback Test-Value" 
    
* System crash and recovery demonstration

    mvn clean spring-boot:run -Dspring-boot.run.arguments="crash Test-Value"
    
    mvn spring-boot:run -Dspring-boot.run.arguments="recovery"

