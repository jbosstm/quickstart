# JTA quickstarts

## Overview

ArjunaJTA is a layer on top of ArjunaCore (which is a general purpose transaction engine and not tied to any particular standard)
and provides full non-distributed implementation of the JTA standard.

## List of quickstarts

These quickstarts showcase the various ways of using JTA:

### [Maven](maven/)

A minimal maven project which shows how to include the narayana JTA artifact
and how to begin and end a JTA style transaction

### [Jakarta transaction](jakarta_transaction/)

Basic example showing how to use the standard JTA APIs

### [Java EE Transactional Application](jee_transactional_app/)

A more advanced example demonstrating how to build transactional JEE applications

### [Object Store](object_store/)

The JTA transaction engine must persist information about participants during transaction processing
in order to guarantee ACID semantics in the event of failures. Narayana provides a number of storage mechanisms
with varying characteristics. The object store quickstarts show how to configure these various stores.

### [Recovery](recovery/)

An important property of transaction systems is the guarantee of consistent state
in the present of varous type of failure. The recovery quickstarts include a number of examples
that demonstrated how the narayana transaction manager possesses this property.

## How to use

`cd` into the required quickstart and follow the `README.md` or call the `run.[sh|bat]` file
