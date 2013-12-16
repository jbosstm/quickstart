
OVERVIEW
--------
The Java Transaction Service (JTS) is the JEE specification for performing distributed transactions and "supports the JTA specification at the high-level and implements the Java mapping of the OMG Object Transaction Service (OTS) 1.1 Specification at the low-level" (quote from the JEE spec). Our implementation of JTS builds on ArjunaCore adding distributed operations and failure recovery. In addition to standards conformance the implementation adds optional capabilities such as nested transactions and interoposition.

Here you will find a number of quickstarts showcasing JTS: 

JTS
-----------------

JEE example showing how to make transactional EJB calls between multiple containers. This quickstart builds on the jta quickstart in the same folder.

JTA
-----------------

Introduces the JTA concepts used in the previous JTS quickstart for the purpose of clarifying what additional steps the application developer needs to apply to make remote EJBs calls transactional.

Recovery
-----------------

Shows standalone JTS (ie the example does not require a JEE application server) and demonstrates how to perform distributed recovery of failed transactions.

Trailmap
-----------

The trail map will help you get started with running Narayna transaction service product in standalone mode and includes a section on jts. The map should be followed in conjunction with the [JTS developers guide](http://docs.jboss.org/jbosstm/latest/guides/narayana-jts-development_guide/index.html#d0e4340)

USAGE
-----
cd into the required quickstart and follow the readme.txt or README.md file
