Standalone JTA with Hibernate example.
==================================================================================================
Author: Gytis Trikleris;
Level: Advanced;
Technologies: JTA, CDI, JPA

What is it?
-----------

This is an example demonstrating how to set up a standalone application which uses Narayana and Hibernate without the application server. It uses Weld to provide CDI capabilities, a standalone JNDI server, Hibernate, and Narayana with its transactional driver to enlist JDBC resources to the transaction.


System requirements
-------------------

To build this project you need Java 8 (Java SDK 1.8) or better and Maven 3.3.3 or better.


Usage
-------------------------------

In your terminal navigate to the quickstart directory and execute one of the following scenarios:

### Commit demonstration

    mvn clean package exec:java -Dexec.args="commit 'Test Value'"

This example executes the following steps:

    1. Prints already existing entries.
    2. Begins a JTA transaction.
    3. Saves entry to the database.
    4. Commits the transaction
    5. Prints all entries in the database.

By the end, you should see one new entry returned from the database.
    
### Rollback demonstration

    mvn clean package exec:java -Dexec.args="rollback 'Test Value'"
     
 This example executes the following steps:
 
     1. Prints already existing entries.
     2. Begins a JTA transaction.
     3. Saves entry to the database.
     4. Rolls back the transaction
     5. Prints all entries in the database.
 
 By the end, you should see that no new entry were added to the database.
    
### System crash and recovery demonstration

    mvn clean package exec:java -Dexec.args="crash 'Test Value'"
    
This part of the example executes the following steps:

    1. Prints already existing entries.
    2. Begins a JTA transaction.
    3. Enlists dummy XA resource.
    4. Saves entry to the database.
    5. Commits the transaction.
    6. Dummy XA resource halts the system between prepare and commit phases.
    
    mvn exec:java -Dexec.args="recovery"
    
NOTE: don't execute clean and/or package steps here, because transaction recovery records will be lost.
This part of the example executes the following steps:

        1. Prints already existing entries.
        2. Starts recovery manager thread.
        3. Waits for the recovery to happen.
        4. Prints all entries in the database.

By the end, you should see dummy XA resource being committed and new entry returned from the database.
        

NOTE: GenericJDBCException visible in the examples is a known issue with hibernate failing to close the connection during the afterCompletion step. (https://issues.jboss.org/browse/JBTM-2676)