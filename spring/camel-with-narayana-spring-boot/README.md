Narayana Spring Boot camel example
============================
Author: Amos Feng
Level: Intermediate
Technologies: JTA, JMS, JPA, Camel, Spring Boot
Summary: This example shows how to use the camel with the narayana in Spring Boot


What is it?
-----------

It builds three camel routers to demonstrate a simple application.

1. read a message from the foo queue and route it to the users queue and the jpa with the database
2. read a message from the users queue and print it
3. read a message from the DLQ queue and print it when the transaction is rollback


Requirements
------------

In order to start the application you needs Java 8.0 or later and Maven 3.2 or later.


Usage
-----

1. Commit Example

In a separate terminal window start the application:

    mvn clean spring-boot:run

Get users:

    curl -X GET "http://localhost:8080/users"

Create a normal user:

    curl -X POST "http://localhost:8080/user" -d 'name=test'

Get the Result

    2017-06-09 22:15:34.367  INFO 8778 --- [msConsumer[foo]] route1: Inserted new User 1 with Name test
    2017-06-09 22:15:34.376  INFO 8778 --- [Consumer[users]] route2: Get user with name test

2. Rollback Example

Create a user with the bad name

    curl -X POST "http://localhost:8080/user" -d 'name=bad'

Get the Result

    2017-06-09 22:15:33.326  INFO 8778 --- [msConsumer[foo]] route1: Inserted new User 1 with Name bad
    2017-06-09 22:15:33.339 ERROR 8778 --- [msConsumer[foo] o.a.camel.processor.DefaultErrorHandler  : Failed delivery for (MessageId: ID:1990d822-4d1e-11e7-86bf-e8b1fc0a8494 on ExchangeId: ID-192-168-0-107-44487-1497017731292-0-1). Exhausted after delivery attempt: 1 caught: java.lang.IllegalArgumentException: bad name
    2017-06-09 22:15:33.346  WARN 8778 --- [msConsumer[foo]] o.a.c.s.spi.TransactionErrorHandler      : Transaction rollback (0x445f6f73) redelivered(false) for (MessageId: ID:1990d822-4d1e-11e7-86bf-e8b1fc0a8494 on ExchangeId: ID-192-168-0-107-44487-1497017731292-0-1) caught: java.lang.IllegalArgumentException: bad name
    2017-06-09 22:15:33.348  WARN 8778 --- [msConsumer[foo]] o.a.c.c.jms.EndpointMessageListener      : Execution of JMS message listener failed. Caused by: [org.apache.camel.RuntimeCamelException - java.lang.IllegalArgumentException: bad name]
2017-06-09 22:15:33.359  INFO 8778 --- [msConsumer[DLQ]] route3: Can not process user with name bad

3. Recovery Example

Create user with the name of halt to trigger the application crash

    curl -X POST "http://localhost:8080/user" -d 'name=halt'

The application will crash

Re-run the application with "-Drecover=true" (notice don't use the "clean")

    mvn spring-boot:run -Drecover=true

Get the Result and you will see the user "halt" will be create

    curl -X GET http://localhost:8080/users
