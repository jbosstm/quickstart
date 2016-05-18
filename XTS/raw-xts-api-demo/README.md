Author: Ivo Studensky <istudens@redhat.com>


Note: This demo has been written for, and tested on, WildFly 10.0.0.Final and WildFly 10.1.0.Final-SNAPSHOT. It may work with other versions of WildFly. Please download and install this app server from the WildFly website and set the appropriate Narayana version in the pom.xml. You can check which Narayana version was used in each WildFly tag over here: https://github.com/wildfly/wildfly/blob/10.0.0.Final/pom.xml.


Note: The java.awt.headless property is needed for the demo as it starts a Java Swing user interface for each or the participant services. This is done for demonstration purposes; it is unlikely that a typical JEE application will ever do this. 


# About the sample application

The sample application features some simple transactional Web services, a client application, deployment metadata files and a build script. The application is designed to introduce some of the key features of the XML Transaction component of Narayana and help you get started with writing your own transactional Web services applications.

The application is based around a simple booking scenario. The services provide the ability to transactionally reserve resources, whilst the client provides an interface to select the nature and quantity of the reservations. The chosen application domain is services for a night out.

The server components consist of three Web services (Restaurant, Theatre, Taxi) which offer transactional booking services. These services each expose a GUI with state information and an event trace log.

The client side of the application is a servlet which allows the user to select the required reservations and then books a night out by making invocations on each of the services within the scope of a Web Services transaction.

Full source code for the services and the client is included, along with a Maven script for building and deploying the code. The following step of this trail map will show you how to deploy and run the application.


# Installation Content

You will require a Web services platform on which to deploy and run the product. This release of the XML Transaction component of Narayana is designed to run within WildFly.

To compile, deploy and run the sample application we also recommend using Java SDK 1.7 and Apache Maven 3.3.3 or later. If you do not already have these, you can download them from java website and the Maven website.


# Deploying the sample application

To run the sample application, you must compile the source code; bundle it, along with the required metadata files, into appropriate deployment constructs and then deploy these into the application container. This process is somewhat involved, but fortunately is completely automated by an Maven build script.

To proceed, you will need to install Maven to take advantage of the supplied build file.


## Deploying into Wildfly:

1. Go to the Wildfly directory:

        cd $JBOSS_HOME
        
2. Copy XTS subsystem's configuration file:

        cp docs/examples/configs/standalone-xts.xml standalone/configuration/

3. Go to the quickstart's home directory and build it:
        
        mvn clean install
        
4. Start Wildfly from WildFly directory with enabled XTS subsystem:

        ./bin/standalone.sh -c standalone-xts.xml -Djava.awt.headless=false
        
5. Deploy the quickstart from the quickstart's home directory:

        mvn -f ear/pom.xml wildfly:deploy
        
6. Open a browser and enter the xts-demo url (e.g. http://localhost:8080/xts-demo) 


## Using the application

When invoked, the client will attempt to begin a transaction, reserve theatre tickets, a restaurant table and a taxi according to the parameters you have selected, then commit the transaction. It will log each step of its activity to the console window. As the transaction proceeds, each of the Web Services will pop up a window of its own in which its state and activity log can be seen. Some events in the service code are also logged to the console.

The three server applications support a manual transaction control mode which you can use to simulate transaction failures. Use the Change Mode button on the server GUIs. Notice that the client throws an exception if the transaction is rolled back. [ Note: The manual commit mode overrides the normal availability checks in the services, so overbooking may occur. ]

The following pages explain the two transaction models available in the XML Transaction , Atomic Transactions and Business Activities. Reading the following pages will help you understand the events taking place within the sample application.


# Atomic Transactions

Atomic transactions are the classical transaction type found in most enterprise data systems, such as relational databases. Atomic transactions typically exhibit ACID properties (Atomic, Consistent, Isolated and Durable). This is usually achieved by the transactions holding locks on data, particularly during transaction resolution through the two phase commit protocol (2PC). In J2EE applications, such transactions are normally managed through the JTA interface, or implicitly by the application container in the case of e.g. certain EJB configurations. Because of their lock based nature, atomic transactions are best suited to short lived operations within the enterprise.

Long lived transactions can exhibit poor concurrency when holding locks for a prolonged period. For the same reason, use of lock based transactions for inter-enterprise integration is avoided due to the possibility of denial of service situations based on incorrect lock management. The next section of the trail map explains how these problems can be addressed through the use of an extended transaction model, Business Activities.

To use the Atomic Transaction transaction type in the sample application, simply select it from the pull down menu at the top of the client interface. Notice that the server applications show the reservation resources (e.g. seats, tables) passing though a lifecycle involving the initial state (free), reserved (locked) and booked (committed).


# Business Activities

Business activities are an extended transaction model designed to support long running business processes. Unlike traditional atomic transactions, business activities typically use a compensation model to support the reversal of previously performed work in the event of transaction cancellation (rollback). This makes them more suitable for long duration processes and inter-enterprise coordination. However, it also requires the relaxation of traditional ACID properties, particularly isolation.

The programming of business activities can involve more effort than is required for atomic transactions, as less infrastructure is typically available. For example, the XA support found in many enterprise databases handles the necessary locking, 2PC and other functions transparently, allowing databases to be used in atomic transactions with minimal programmer effort. However, equivalent support for business activities, particularly with regard to compensation logic, must be added to the code of each new application by the programmer.

The demonstration application illustrates one possible approach to creating services for use in business activities. It shows how to create a transaction participant that can expose existing business logic, originally intended for use in atomic transactions, as a service suitable for use in a business activity. This is a particularly common scenario for enterprises seeking to reuse existing logic by packaging it for use as a component in the composition of workflow type processes.

To use the Business Activity transaction type in the sample application, simply select it from the pull down menu at the top of the client interface. Notice that the client applications show the reservation resources as booked (committed) even before the transaction is terminated, subsequently performing a compensating transaction to reverse this effect if the transaction is cancelled.


# Source code overview

You can begin experimenting with the XML Transaction component of Narayana by editing the sample application source code, which is heavily commented to assist your understanding. The source code can be found in the <DEMO_HOME>/src directory. Deployment descriptors for the application can be found iin directory <DEMO_HOME>/dd.

It is structured as follows:


* com/jboss/jbosstm/xts/demo/

  * client/BasicClient.java:

    A servlet that processes the form input and runs either an Atomic Transaction (AT) or Business Activity (BA) to make the bookings.
    
    This servlet uses the JBossWS JaxWS implementation as the SOAP transport library.

    Method configureClientHandler installs the JBoss handler on the JaxWS service endpoint proxies. This ensurs that the client's AT or BA transaction context is propagated to the web services when their remote methods are invoked.

  * restaurant/* :

    JaxWS client interfaces for accessing the remote restaurant web services via JaxWS service proxies.

  * taxi/* :

    JaxWS client interfaces for accessing the remote taxi web services via JaxWS service proxies.

  * theatre/* :

    JaxWS client interfaces for accessing the remote theatre web services via JaxWS service proxies.

  * services/[restuarant|taxi|theatre]/* :

    JaxWS service endpoint implementation classes

    Each of these three Web services has similar structure, featuring a *Manager.java class (the transactional business logic, knowing nothing of Web services), a *View.java file (the GUI component, largely tool generated), and the files that expose the business logic as transactional JaxWS Web services.

    In the filenames, AT denotes Atomic Transaction, whilst BA is for Business Activities.

    The *ServiceAT/BA.java file is the business interface, whilst the *Participant.java file has the transaction management code.

    The *ServiceAT/BA classes expose their JaxWS SEI methods using 'javax.jws.WebService' and 'javax.jws.WebMethod' annotations.

    A 'javax.jws.HandlerChain' annotation identifies a handler chain deployment descriptor file deployed with the demo applciation. This decriptor configures the services with handlers that run SEI method invocations in the transaction context propagated from the client.


# Basic test coverage for Narayana XTS Demo App

This is some basic test coverage for the XTS Demo App. It is powered by Arquillian
and its Drone extension. By default it calls a local Firefox browser to execute the tests.

The test starts up the AS, deploys the XTS Demo App into it and runs a reservation of some
restaurant/theatre/taxi seats using both AtomicTransaction and BusinessActivity tx type.
