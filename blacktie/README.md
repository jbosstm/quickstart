
OVERVIEW
--------

BlackTie is an [XATMI](http://pubs.opengroup.org/onlinepubs/9694999399/toc.pdf) implmentation. XATMI is a set of APIs for writing distributed client/server applications. BlackTie bridges the gap between JEE and XATMI and provides the tools to allow existing XATMI application programs written in C/C++ to integrate seamlessly into the wildfly/JBoss stack allowing these applications to migrate easily to the services provided by the wildfly application server. As well as providing C++ APIs to support the X/Open XATMI specification, Blacktie also provides the JAtmiBroker API to support clients and services written in Java. Here you find a variety of quickstarts to get you started using BlackTie. Note that it should integrate with the lastest wildfly master.

xatmi
-----

Quickstarts relating to the XATMI standard such as security, interacting with databases, making remote service calls, using queues and topics. This is the best place to start to get a basic understanding of BlackTie and XATMI.

blacktie-admin-services
-----------------------

Shows how to use the admin server to perform various administrative operatons on BlackTie domains and servers such as:
- listing servers in a domain
- Halt servers, update configuration, restart
- pause and resume a domain
- etc

- listing services
- advertising/unadvertising services
- stopping/starting servers
- etc

integration1
------------

Shows how a C client can invoke an XATMI service and an EJB within the scope of the same transaction

jatmibroker
-----------

An example of how to use Java based XATMI services

messaging
---------

Demonstrates messaging support in BlackTie

nbf
---

Shows how to use Nested Buffers for using complex datatypes in services

USAGE
-----
cd into the relevant quickstart directory and browse the readme file.
