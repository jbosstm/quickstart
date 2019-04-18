# Quickstarts

## Introduction

The repository contains a set of quickstart projects which provide working example
for specific Narayana transaction manager capabilities.
The quickstarts can be used as a reference for your own project.

The list of all available quickstart can be found down at this page.
Each quickstart is categorized with tags that depict areas the quickstart is connected with.

* [List of quickstarts](#list-of-quickstarts)
* [Quickstarts by tag](#quickstarts-by-tag)
* [Contacting us](#contacting-us)
* [Quickstarts in CI environment](#quickstarts-in-ci-environment)
* [Running a single quickstart](#running-a-single-quickstart)
* [Running all quickstarts in a single command](#running-all-quickstarts-in-a-single-command)


---
**NOTE**

WildFly quickstarts contain some more examples how to work with transactions
in the Java EE environment. See repository: [wildfly/quickstart](https://github.com/wildfly/quickstart).
The quickstarts particularly connected to transaction managements are:
[jta-crash-rec](https://github.com/wildfly/quickstart/tree/master/jta-crash-rec),
[jts-distributed-crash-rec](https://github.com/wildfly/quickstart/tree/master/jts-distributed-crash-rec),
[jts](https://github.com/wildfly/quickstart/tree/master/jts).

---

## List of quickstarts

Project name | Description | Maven coordinates | Tags
------------ | ----------- | ----------------- | ----
[ArjunaCore/txoj](ArjunaCore/txoj/) | Showing how to use the Transactional Object for Java (_TXOJ_) which is the core implementation concept of the Narayana state machine | _o.j.n.quickstart.arjunacore :txoj_ | _[arjuna.core](#arjuna-core-tag), [standalone](#standalone-tag)_
[ArjunaJTA/maven](ArjunaJTA/maven) | Minimalistic maven project showing how to configure maven to use the Narayana JTA implementation | _o.j.n.quickstart.jta :maven_ | _[jta](#jta-tag), [standalone](#standalone-tag)_
[ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/) | basics on using JTA API in your application, you can check how to obtain the `UserTransaction` and `TransactionManager` with use of the Narayana API | _o.j.n.quickstart.jta :javax_transaction_ | _[jta](#jta-tag), [standalone](#standalone-tag)_
[ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/) | WildFly application which shows use of the transaction management in EJB when invoked from CDI bean | _o.j.n.quickstart.jta: jee_transactional_app_ | _[jta](#jta-tag), [wildfly](#wildfly-tag)_
[ArjunaJTA/object_store](ArjunaJTA/object_store/) | How to configure Narayana to run different types of object stores in standalone mode | _org.jboss.narayana.quickstart.jta: object_store_ | _[jta](#jta-tag), [narayana.configuration](#narayana-configuration-tag), [standalone](#standalone-tag)_
[ArjunaJTA/recovery](ArjunaJTA/recovery/) | Example of running periodic recovery in Narayana standalone. The setup shows multiple implementation of the recovery storage for could be configured by user. | _o.j.n.quickstart.jta: recovery_ | _[jta](#jta-tag), [narayana.configuration](#narayana-configuration-tag), [standalone](#standalone-tag)_
[ArjunaJTS/interop](ArjunaJTS/interop/) | Transactional EJB calls between GlassFish and WildFly | _o.j.n.quickstart.jts :jts-interop-quickstart_ | _[jts](#jts-tag),[wildfly](#wildfly-tag),[glassfish](#glassfish-tag)_
[ArjunaJTS/jts](ArjunaJTS/jts/) | Demonstration of setting up JTS transactions in WildFly and how to use EJB2 beans to pass the transactional context over the remote IIOP call | _o.j.n.quickstart.jts :jboss-as-jts-parent_ | _[jts](#jts-tag),[wildfly](#wildfly-tag)_
[ArjunaJTS/recovery](ArjunaJTS/recovery/) | Setting up the Narayana recovery manager for JTS in standalone mode | _o.j.n.quickstart.jts :jts-recovery_ | _[jts](#jts-tag), [narayana.configuration](#narayana-configuration-tag), [standalone](#standalone-tag)_
[ArjunaJTS/standalone](ArjunaJTS/standalone/) | Example of using Narayana JTS and in second part the example of using ORB API to manage remote JTS transaction manager with IIOP calls | _o.j.n.quickstart.jts :jts-standalone_ | _[jts](#jts-tag), [iiop](#iiop-tag), [standalone](#standalone-tag)_
[atsintegration](atsintegration/) | How to integrate Narayana with and arbitrary Java EE application server | _o.j.n.quickstart.jta :application-server-integration_ | _[narayana.configuration](#narayana-configuration-tag)_
[agroal](agroal/) | How to integrate Narayana with Agroal jdbc pooling library | _o.j.n.quickstart.jta :agroal_ | _[jta](#jta-tag), [standalone](#standalone-tag), [jdbc](#jdbc-tag)_
[jta-1_2-standalone](jta-1_2-standalone/) | How to integrate Narayana with Weld | _o.j.n.quickstart.jta :jta-1_2-standalone_ | [standalone](#standalone-tag), [cdi](#cdi-tag)
[jta-1_2-in-wildfly](jta-1_2-in-wildfly/) | How to use transactions with CDI in WildFly | _o.j.n.quickstart.jta :jta-1_2-in-wildfly_ | [standalone](#standalone-tag), [cdi](#cdi-tag)
[jta-and-hibernate-standalone](jta-and-hibernate-standalone/) | Using Narayana to manage transactions when JPA and CDI is used | _o.j.n.quickstart.jta :jta-and-hibernate-standalone_ | [standalone](#standalone-tag), [cdi](#cdi-tag)

## Quickstart categorization

Category | List of quickstarts
-------- | -------------------
**[arjuna.core](#arjuna-core-tag-definition)**<a name='arjuna-core-tag'> | [ArjunaCore/txoj](ArjunaCore/txoj/)
**[narayana.configuration](#narayana-configuration-tag-definition)**<a name='narayana-configuration-tag'> | [ArjunaJTA/object_store](ArjunaJTA/object_store/), [ArjunaJTA/recovery](ArjunaJTA/recovery/), [ArjunaJTS/recovery](ArjunaJTS/recovery/), [atsintegration](atsintegration/)
**[jta](#jta-tag-definition)**<a name='jta-tag'> | [ArjunaJTA/maven](ArjunaJTA/maven/), [ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/), [ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/), [ArjunaJTA/object_store](ArjunaJTA/object_store/), [ArjunaJTA/recovery](ArjunaJTA/recovery/), [agroal](agroal/)
**[jts](#jts-tag-definition)**<a name='jts-tag'> | [ArjunaJTS/interop](ArjunaJTS/interop/), [ArjunaJTS/jts](ArjunaJTS/jts/), [ArjunaJTS/recovery](ArjunaJTS/recovery/), [ArjunaJTS/standalone](ArjunaJTS/standalone/)
**[standalone](#standalone-tag-definition)**<a name='standalone-tag'> | [ArjunaCore/txoj](ArjunaCore/txoj/), [ArjunaJTA/maven](ArjunaJTA/maven/), [ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/), [ArjunaJTA/object_store](ArjunaJTA/object_store/), [ArjunaJTA/recovery](ArjunaJTA/recovery/), [ArjunaJTS/recovery](ArjunaJTS/recovery/), [ArjunaJTS/standalone](ArjunaJTS/standalone/), [agroal](agroal/), [jta-1_2-standalone](jta-1_2-standalone/), [jta-and-hibernate-standalone](jta-and-hibernate-standalone/)
**[wildfly](#wildfly-tag-definition)**<a name='wildfly-tag'> | [ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/),[ArjunaJTS/interop](ArjunaJTS/interop/), [ArjunaJTS/jts](ArjunaJTS/jts/), [jta-1_2-in-wildfly](jta-1_2-in-wildfly/)
**[glassfish](#glassfish-tag-definition)**<a name='glassfish-tag'> | [ArjunaJTS/interop](ArjunaJTS/interop/)
**[iiop](#iiop-tag-definition)**<a name='iiop-tag'> | [ArjunaJTS/standalone](ArjunaJTS/standalone/)
**[jdbc](#jdbc-tag-definition)**<a name='jdbc-tag'> | [agroal](agroal/)
**[cdi](#cdi-tag-definition)**<a name='cdi-tag'> | [jta-1_2-standalone](jta-1_2-standalone/), [jta-1_2-in-wildfly](jta-1_2-in-wildfly/), [jta-and-hibernate-standalone](jta-and-hibernate-standalone/)

### Tags definition

* **arjuna.core**<a name='arjuna-core-tag-definition'> : demonstrating capabilities of Narayana API,
  it's helpful for developers want to write a transaction state machine
  and don't want to start on a green field but rather used battle tested library
* **narayana.configuration**<a name='narayana-configuration-tag-definition'> : depicting aspects
  of Narayana configuration and showing options of such configurations
* **jta**<a name='jta-tag-definition'> : using JTA API to demonstrate transaction processing
* **jts**<a name='jts-tag-definition'> : using JTS API to demonstrate how the Narayana transaction system
  could be run and configured to run distributed JTS transactions
* **standalone**<a name='standalone-tag-definition'> : running as standalone Java SE application
* **wildfly**<a name='wildfly-tag-definition'> : running as deployment on WildFly application server
* **glassfish**<a name='glassfish-tag-definition'> : running on GlassFish application server
* **iiop**<a name='iiop-tag-definition'> : showing how to use ORB API with transaction manager
* **jdbc**<a name='jdbc-tag-definition'> : using JDBC api and showing integration with that
* **cdi**<a name='cdi-tag-definition'> : showing how to use the CDI to be integrated with JTA


## Contacting us

We are always happy to talk transactions and how-to use Narayana in exotic and not so exotic environments.
If you have ideas for what we can add to the quickstarts to make them more useful please do reach out to us at:
http://narayana.io/community

## Quickstarts in CI environment

If you want to see how we run the quickstarts in our continuous integration environment, take a look at [scripts/hudson/quickstart.sh](scripts/hudson/quickstart.sh).

## Running a single quickstart

Change directory into the required quickstart and follow the instructions in the [README.md](README.md) file.

## Running all quickstarts in a single command

To run the quickstarts:

1. set `WORKSPACE` (to the root of the quickstart checkout)
2. set `JBOSSAS_IP_ADDR` (default is `localhost`)
3. set `JBOSS_HOME` (to the path of WildFly server, you can download the server at http://wildfly.org/downloads)
4. `mvn clean install`

_NOTE:_
One of the BlackTie quickstarts requires the Oracle driver to be downloaded and configured,
see [blacktie/test/initializeBlackTie.xml](blacktie/test/initializeBlackTie.xml) for more details.

It is disabled by default but running `./blacktie/run_all_quickstarts.[sh|bat] tx` will execute it.

_NOTE:_
As the scope of Narayana quickstart is broad - it shows integration with many platform
&ndash; you need to expect a big amount of data to be downloaded from Maven repository.

_NOTE:_
If you want to prepare the quickstarts by installing them without running any test
then run the `mvn clean install -DskipTests`

_NOTE:_
If you want to run the quickstart with specific Narayana version you can use
command line option `-Dversion.narayana=...`
