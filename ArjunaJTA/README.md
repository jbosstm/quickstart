
OVERVIEW
--------
ArjunaJTA is a layer on top of ArjunaCore (which is a general purpose transaction engine and not tied to any particular standard) and provides full non-distributed implementation of the JTA standard.

These quickstarts showcase the various ways of using JTA: 

maven
-----

A minimal maven project which shows how to include the narayana JTA artifact and how to begin and end a JTA style transaction

javax transaction
-----------------

Basic example showing how to use the standard JTA APIs

JEE Transactional App
-----------------

A more advanced example demonstrating how to build transactional JEE applications

Standalone JTA 1_2
-----------------

JTA 1.2 introduces new annotations for controlling the transactional behaviour of CDI beans. The standalone-jta-1_2 quickstart indicate how to take advantage of these annotations in a standalone application (ie in environments where a JEE container is either not available or is undesirable) 

Object Store
-----------------

The JTA transaction engine must persist information about participants during transaction processing in order to guarantee ACID semantics in the event of failures. Narayana provides a number of storage mechanisms whith varying characteristics. The object store quickstarts show how to configure these various stores.

Recovery
-----------------

An important property of transaction systems is the guarantee of consistent state in the present of varous type of failure. The recovery quickstarts include a number of examples that demonstrated how the narayana transaction manager possesses this property.


USAGE
-----
cd into the required quickstart and follow the readme.txt or call the run.[sh|bat] file
