TXFramework Quickstarts
=======================
Author: Paul Robinson

_NOTE: These quickstarts utilise technology that is in status "Tech Preview". Although each quickstart is tested regularly, you may still find issues when developing your own applications using the TXFramework. We are very keen to hear about your experiences with the TXFramework and of any bugs you may find. Please direct these to the Narayana forum, here: https://community.jboss.org/en/jbosstm.

The TXFramework provides annotation support for writing transactional applications. The same set of annotations can be used to
implement applications using any of the supported transaction protocols. In addition to the JTA and JTS transaction
support already offered by JBoss AS, the TXFramework also supports, WS-AT, WS-BA and REST-AT protocols.

Traditionally, when writing applications that use WS-AT, WS-BA or REST-AT, the developer has needed to write a lot of boiler
plate code. This makes it harder to learn how to write such applications and makes development more error prone and
increases the maintenance burden. It also makes it a lot harder to refactor an application from using one transport type
to another (E.g Web services to REST). The TXFramework removes all of this boiler-plate code and replaces it with a small set
of annotations. Where possible, the annotations make use of sensible defaults, so developing standard applications
requires very few annotations. More complex scenarios can be supported through additional annotations and optional fields
on the required annotations.

Applications are written using an abstract set of annotations. These annotations bind the application to either an ACID or a
compensation based transaction model. The application is not bound to the particular transaction model used, such as WS-AT
or REST-AT. Through annotations, the application may optionally hook into the lifecycle of the transaction protocol and
be notified when particular events occur. For example, an application using an ACID model may need to participate in a
synchronization protocol. This can be done by simply annotating methods that you wish to be invoked during the
lifecycle with appropriate annotations. As well as improving developer usability, this approach also makes it trivial to
switch the incoming transaction protocol. For example. an application can be switched from using WS-AT to REST-AT by simply
removing the standard JAX-WS annotations and replacing them with standard JAX-RS annotations.

The TXFramework also supports bridging between supported protocols. This allows an application to offer remote access
using whichever technology is most appropriate, whilst still using JTA for access to backend resources, such as a database
or messaging server. For example, the application may be exposed as a WS-AT enabled Web service which works with
a database. In this scenario, the TXFramework would set up a transaction bridge between the the WS-AT and JTA transactions.
The two transactions would then be joined into a single distributed transaction. Bridging can also be done from the client;
here calls to WS-AT enabled Web services are bridged from any existing JTA transaction that may be running on the client.
This allows the client to work with a local database or messaging server and include a remote call to a Web service in the
same transaction. 

Currently, the TXFramework only supports bridging between JTA to WS-AT and WS-AT to JTA. Support for bridging between
REST-AT and JTA is planned for the near future. We are also investigating what bridging support is needed to combine
multiple ACID (JTA) transactions into a single compensation based transaction, such as that offered by WS-BA.


Limitations
===========

As stated above, the TXFramework is currently in status "Tech Preview". Although, we have designed many of its features,
the implementation is still in the early stages of development. We feel it is very important to get community feedback,
hence the reason why we are making this technology available at such an early stage. We welcome any feedback you may have;
the best place provide this feedback is on the Narayana forum here: https://community.jboss.org/en/jbosstm. You can also
track the progress and roadmaping of particular features by consulting the JBTM jira project here: https://issues.jboss.org/browse/JBTM
TXFramework related issues are all assigned the component "TXFramework". 

The most note-worthy limitations are as follows:

1. No REST-AT bridging.
2. No WS-BA to JTA bridging.
3. WS-AT and WS-BA client side interceptors are not yet added automatically.
4. JTA to WS-AT bridging still needs to be set up manually, by adding SOAP interceptors to the client stub.
5. Documentation is currently limited to just these quickstarts.
6. Recovery is not yet supported.

Pre Requisites
==============

The TXFramework is not currently shipped with JBoss AS, therefore you will need to build it from source or download a pre-built version. 


Download Pre-built
==================

Download the pre-built bundle of jboss-as-7.2.0.Alpha1-SNAPSHOT containing narayana-5.0.0.M2-SNAPSHOT. The bundle was built using the commands in the next section on 22/06/2012.

    wget http://bit.ly/Mr0QgD


Build From Source
=================

Build Narayana from source

    git clone git://github.com/jbosstm/narayana.git
    cd narayana
    git checkout master
    ./build.sh -DskipTests=true

Obtain the source for the TXFramework enabled JBossAS and build it:

    git clone git://github.com/jbosstm/jboss-as.git
    cd jboss-as
    git checkout 5_BRANCH
    mvn install -DskipTests=true

You will find the built version of JBoss here:

    ls ./build/target


Configure
=========

Now set your JBOSS_HOME environment variable to the location of the built JBoss AS. For example:

    export JBOSS_HOME=~/dev/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT
    cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration/

You now have a build of JBossAS enabled for use with the TXFramework and you are now ready to run each of the quickstarts. Make sure you have a local copy of these quickstarts:

    cd ..
    git clone git://github.com/jbosstm/quickstart.git
    cd quickstart/TXFramework
