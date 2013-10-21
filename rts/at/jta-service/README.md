REST-AT with JTA: Example of Using REST-AT with JPA and JMS
======================================================
Author: Gytis Trikleris (gytis@redhat.com)
Technologies: REST-AT to JTA Bridge, JPA, JMS

What is it?
-----------
This example demonstrates the use of bridging REST-AT to JTA with services using JPA and JMS.

Quickstart consists of two modules: JPA and JMS. Both of them are completely independent and can be executed separately.

 * JPA module allows creating users and tasks through Restfull API.

 * JMS module allows sending messages to the queue and receiving messages from it.

Build and Deploy the Quickstart
-------------------------

In order to run quickstarts in the managed Wildfly application server, run the following command:

        mvn clean install

In order to run quickstarts in the remote Wildfly application server, run the following command:

        mvn clean install -Parq-jbossas-remote

Test output can be found in quickstarts surefire-report directories, if managed application server was used. And in Wildfly server log, if remote application server was used.