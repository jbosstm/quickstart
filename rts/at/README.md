RESTful Transactions Quickstarts
================================

Examples using RESTAT (RESTful Atomic Transactions).

OVERVIEW
========

All the quickstarts related to rts are found in this folder structure. Each quickstart has
a README.md file explaining how to execute the quickstart and what is happening in it. Each quickstart
also contains a script for automating the execution of the quickstart.

<a id="usage"></a>
USAGE
=====

Each example needs a RESTAT coordinator for begining and ending transactions. Before running any of the
examples you must make sure that the RESTAT coordinator is deployed. If you have access to the
wildfly application server then the RESTAT coordinator can be deployed out of the box by starting
the server with the RTS subsystem enabled using an optional server configuration:

    Linux: ./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-rts.xml
    Windows:   bin\standalone.bat --server-config=..\..\docs\examples\configs\standalone-rts.xml

For other versions of the application server you will need to deploy the coordinator as a war archive.
The archive is contained in the bin folder of the narayana download (restat-web.war). If you are building
from source the archive is located in rts/at/webservice/target/restat-web-<version>.war.

The coordinator endpoint is different depending on which way you deploy the coordinator. For wildfly use:

    http://localhost:8080/rest-at-coordinator/tx/transaction-manager

otherwise the endpoint is

    http://localhost:8080/rest-tx/tx/transaction-manager

The endpoint is defined in the maven pom of each quickstart and defaults to the first form. If you use
anything other that the default (for example if you start the app server on a different host or port)
you will need modify the quickstarts pom (look for a line starting with <argument>coordinator=http ...).

To run a quickstart change directory into the required quickstart and follow the instructions in
the README.md file or call the run.[sh|bat] file.

The available quickstarts are a follows:


Simple
======

Minimal "getting started" example showing how to start and end a transaction using REST style semantics

Service
=======

A more advanced example which shows how to make your web services transactional. Two versions of the
quickstart are provided, the first uses HTTP calls to start/end and enlist resources whilst the second version
uses an integration API which simplifies writing of participants. These two quickstarts deploy a transactional
service into an embedded JAX-RS container. We also provide a alternate version of each which shows the same
behaviour but with the service deployed into an external (wildfly) JAX-RS container.


JTA Service
===========
This quickstart demonstrates how a Web Service using JTA can participate in a REST-AT transaction. The example uses JPA
to update a database and JMS to send a message.

Recovery
========

An example which builds on the service quickstarts and demonstrates that web services which use RESTAT
remain consistent in the presence of failures. Two versions of the quickstart are provided, the second
version uses an integration API which simplifies writing of participants.

Demonstrator
============

An example using the ruby language showing how to start and end a transaction using REST style semantics
and how services can recover from failures during the commitment protocol.

