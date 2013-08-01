This example is functionally equivalent to the service1 quickstart. The service1 example deployed a JAX-RS service to an
embedded container whereas this example deploys the same service into the wildfly application server.

USAGE
-----
Prior to running the example make sure that the [RESTAT coordinator is deployed](../../README.md#usage).

Note that version 8.0.0.Alpha3 contains a RESTAT profile so you do not need to deploy the RESTAT coordinator manually.
You do however need to declare a dependency on the RESTAT subsystem in the war manifest
(you can find the manifest in the src/main/resources/META-INF directory).

Deploy the service into a running wildfly applications server:

    mvn clean package jboss-as:deploy

Now run a client which will start a RESTAT transaction and make calls on the deployed service:

    mvn -P client compile exec:exec

