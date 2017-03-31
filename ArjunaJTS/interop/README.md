OVERVIEW
--------

This example shows how to propagate a transaction context between the WildFly and GlassFish application
servers. There is a single script to run the full example but you may find it more instructive to run
the example in steps perhaps in separate command windows:

USAGE
-----

./run.sh

or to run each step at a time:

step1.sh # build WildFly and GlassFish (including checking out the repo) and the demo EJBs
step2.sh # configure WildFly to use JTS and start it
step3.sh # start GlassFish
step4.sh # deploy demo EJBs to WildFly and GlassFish
step5.sh # perform a transactional EJB call from GlassFish to WildFly
step6.sh # perform a transactional EJB call from WildFly to GlassFish 
step7.sh # shutdown GlassFish and WildFly


EXPECTED OUTPUT
---------------

This output is from running steps 2 to 7 (step 1 is is not show since it is quite verbose).

The key lines to verify that the GlassFish to WildFly transactional EJB call worked is to look for the line:
===== JTS interop quickstart: step 5 (EJB call gf -> wf):

followed soon after by

Next: 8000

The key lines to verify that the WildFly to GlassFish transactional EJB call worked is to look for the line:
===== JTS interop quickstart: step 6 (EJB call wf -> gf):

followed soon after by

Next: 7000

An example of the full output for steps 2 to 7 is: 

[mmusgrov@dev1 quickstart](JBTM-2874)$ PROFILE=MAIN ./scripts/hudson/quickstart.sh
Running quickstarts
[INFO] Scanning for projects...
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for org.jboss.narayana.quickstart.rts:simple:jar:5.5.6.Final-SNAPSHOT
[WARNING] 'build.plugins.plugin.version' for org.codehaus.mojo:exec-maven-plugin is missing. @ line 48, column 15
[WARNING] 
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING] 
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] JTS Interoperability Quickstart: GlassFish
[INFO] JTS Examples
[INFO] narayana-quickstarts-all
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building JTS Interoperability Quickstart: GlassFish 5.5.6.Final-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ simple ---
[INFO] Deleting /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/target
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ simple ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ simple ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ simple ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ simple ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ simple ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ simple ---
[WARNING] JAR will be empty - no content was marked for inclusion!
[INFO] Building jar: /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/target/simple-5.5.6.Final-SNAPSHOT.jar
[INFO] 
[INFO] --- exec-maven-plugin:1.5.0:exec (Execute Run Scripts) @ simple ---
===== JTS interop quickstart: step 2 (start WildFly):
=========================================================================

  JBoss Bootstrap Environment

  JBOSS_HOME: /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT

  JAVA: /usr/local/jdk1.8.0_91/bin/java

  JAVA_OPTS:  -server -Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n

=========================================================================

Listening for transport dt_socket at address: 8787
14:20:08,980 INFO  [org.jboss.modules] (main) JBoss Modules version 1.6.0.Beta6
14:20:09,248 INFO  [org.jboss.msc] (main) JBoss MSC version 1.2.7.SP1
14:20:09,338 INFO  [org.jboss.as] (MSC service thread 1-8) WFLYSRV0049: WildFly Core 3.0.0.Beta11 "Kenny" starting
14:20:10,267 INFO  [org.jboss.as.controller.management-deprecated] (Controller Boot Thread) WFLYCTL0028: Attribute 'security-realm' in the resource at address '/core-service=management/management-interface=http-interface' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
14:20:10,292 INFO  [org.jboss.as.controller.management-deprecated] (ServerService Thread Pool -- 29) WFLYCTL0028: Attribute 'security-realm' in the resource at address '/subsystem=undertow/server=default-server/https-listener=https' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
14:20:10,295 INFO  [org.wildfly.security] (ServerService Thread Pool -- 8) ELY00001: WildFly Elytron version 1.1.0.Beta33
14:20:10,366 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0039: Creating http management service using socket-binding (management-http)
14:20:10,381 INFO  [org.xnio] (MSC service thread 1-1) XNIO version 3.5.0.Beta2
14:20:10,386 INFO  [org.xnio.nio] (MSC service thread 1-1) XNIO NIO Implementation Version 3.5.0.Beta2
14:20:10,419 INFO  [org.wildfly.extension.io] (ServerService Thread Pool -- 43) WFLYIO001: Worker 'default' has auto-configured to 16 core threads with 128 task threads based on your 8 available processors
14:20:10,425 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 44) WFLYCLINF0001: Activating Infinispan subsystem.
14:20:10,433 INFO  [org.jboss.as.jaxrs] (ServerService Thread Pool -- 46) WFLYRS0016: RESTEasy version 3.0.22.Final
14:20:10,434 INFO  [org.wildfly.iiop.openjdk] (ServerService Thread Pool -- 45) WFLYIIOP0001: Activating IIOP Subsystem
14:20:10,440 WARN  [org.jboss.as.txn] (ServerService Thread Pool -- 64) WFLYTX0013: Node identifier property is set to the default value. Please make sure it is unique.
14:20:10,451 INFO  [org.jboss.as.security] (ServerService Thread Pool -- 63) WFLYSEC0002: Activating Security Subsystem
14:20:10,456 INFO  [org.jboss.as.security] (MSC service thread 1-6) WFLYSEC0001: Current PicketBox version=5.0.0.Beta1
14:20:10,466 INFO  [org.jboss.as.webservices] (ServerService Thread Pool -- 66) WFLYWS0002: Activating WebServices Extension
14:20:10,466 INFO  [org.jboss.as.naming] (ServerService Thread Pool -- 55) WFLYNAM0001: Activating Naming Subsystem
14:20:10,466 INFO  [org.jboss.as.jsf] (ServerService Thread Pool -- 51) WFLYJSF0007: Activated the following JSF Implementations: [main]
14:20:10,482 INFO  [org.jboss.as.connector.subsystems.datasources] (ServerService Thread Pool -- 39) WFLYJCA0004: Deploying JDBC-compliant driver class org.h2.Driver (version 1.4)
14:20:10,515 INFO  [org.jboss.as.connector] (MSC service thread 1-5) WFLYJCA0009: Starting JCA Subsystem (WildFly/IronJacamar 1.4.2.Final)
14:20:10,515 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0003: Undertow 1.4.11.Final starting
14:20:10,542 INFO  [org.jboss.as.connector.deployers.jdbc] (MSC service thread 1-5) WFLYJCA0018: Started Driver service with driver-name = h2
14:20:10,667 INFO  [org.jboss.as.naming] (MSC service thread 1-7) WFLYNAM0003: Starting Naming Service
14:20:10,668 INFO  [org.jboss.as.mail.extension] (MSC service thread 1-5) WFLYMAIL0001: Bound mail session [java:jboss/mail/Default]
14:20:10,679 INFO  [org.jboss.remoting] (MSC service thread 1-2) JBoss Remoting version 5.0.0.Beta19
14:20:10,759 INFO  [org.jboss.as.ejb3] (MSC service thread 1-2) WFLYEJB0481: Strict pool slsb-strict-max-pool is using a max instance size of 128 (per class), which is derived from thread worker pool sizing.
14:20:10,759 INFO  [org.jboss.as.ejb3] (MSC service thread 1-1) WFLYEJB0482: Strict pool mdb-strict-max-pool is using a max instance size of 32 (per class), which is derived from the number of CPUs on this host.
14:20:10,815 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 65) WFLYUT0014: Creating file handler for path '/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/welcome-content' with options [directory-listing: 'false', follow-symlink: 'false', case-sensitive: 'true', safe-symlink-paths: '[]']
14:20:10,822 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-4) WFLYUT0012: Started server default-server.
14:20:10,824 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-4) WFLYUT0018: Host default-host starting
14:20:10,869 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0006: Undertow HTTP listener default listening on 127.0.0.1:8080
14:20:11,029 INFO  [org.jboss.as.patching] (MSC service thread 1-8) WFLYPAT0050: WildFly cumulative patch ID is: base, one-off patches include: none
14:20:11,072 WARN  [org.jboss.as.domain.management.security] (MSC service thread 1-3) WFLYDM0111: Keystore /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/configuration/application.keystore not found, it will be auto generated on first use with a self signed certificate for host localhost
14:20:11,106 INFO  [org.jboss.as.server.deployment.scanner] (MSC service thread 1-2) WFLYDS0013: Started FileSystemDeploymentService for directory /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/deployments
14:20:11,113 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-3) WFLYSRV0027: Starting deployment of "ejbtest.war" (runtime-name: "ejbtest.war")
14:20:11,168 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-2) WFLYUT0006: Undertow HTTPS listener https listening on 127.0.0.1:8443
14:20:11,197 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221000: live Message Broker is starting with configuration Broker Configuration (clustered=false,journalDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/journal,bindingsDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/bindings,largeMessagesDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/largemessages,pagingDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/paging)
14:20:11,260 INFO  [org.infinispan.factories.GlobalComponentRegistry] (MSC service thread 1-6) ISPN000128: Infinispan version: Infinispan 'Chakra' 8.2.6.Final
14:20:11,269 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221012: Using AIO Journal
14:20:11,322 INFO  [org.jboss.ws.common.management] (MSC service thread 1-2) JBWS022052: Starting JBossWS 5.1.8.Final (Apache CXF 3.1.10) 
14:20:11,350 INFO  [org.wildfly.iiop.openjdk] (MSC service thread 1-5) WFLYIIOP0009: CORBA ORB Service started
14:20:11,420 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-server]. Adding protocol support for: CORE
14:20:11,423 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-amqp-protocol]. Adding protocol support for: AMQP
14:20:11,424 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-hornetq-protocol]. Adding protocol support for: HORNETQ
14:20:11,425 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-stomp-protocol]. Adding protocol support for: STOMP
14:20:11,838 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-5) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor acceptor
14:20:11,839 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-4) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor acceptor
14:20:11,839 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-2) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor-throughput acceptor
14:20:11,839 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-7) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor-throughput acceptor
14:20:11,920 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221007: Server is now live
14:20:11,920 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221001: Apache ActiveMQ Artemis Message Broker version 1.5.3.jbossorg-003 [default, nodeID=d7881823-1604-11e7-89ce-e8b1fc470d10] 
14:20:11,937 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 68) WFLYMSGAMQ0002: Bound messaging object to jndi name java:jboss/exported/jms/RemoteConnectionFactory
14:20:11,947 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 70) WFLYMSGAMQ0002: Bound messaging object to jndi name java:/ConnectionFactory
14:20:11,948 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 69) AMQ221003: Deploying queue jms.queue.ExpiryQueue
14:20:11,954 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 71) AMQ221003: Deploying queue jms.queue.DLQ
14:20:11,975 INFO  [org.jboss.as.ejb3] (MSC service thread 1-7) WFLYEJB0493: EJB subsystem suspension complete
14:20:12,093 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-4) WFLYJCA0001: Bound data source [java:jboss/datasources/ExampleDS]
14:20:12,096 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-3) WFLYJCA0007: Registered connection factory java:/JmsXA
14:20:12,117 INFO  [org.jboss.weld.deployer] (MSC service thread 1-5) WFLYWELD0003: Processing weld deployment ejbtest.war
14:20:12,170 INFO  [org.apache.activemq.artemis.ra] (MSC service thread 1-3) Resource adaptor started
14:20:12,171 INFO  [org.jboss.as.connector.services.resourceadapters.ResourceAdapterActivatorService$ResourceAdapterActivator] (MSC service thread 1-3) IJ020002: Deployed: file://RaActivatoractivemq-ra
14:20:12,172 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-2) WFLYJCA0002: Bound JCA ConnectionFactory [java:/JmsXA]
14:20:12,173 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-2) WFLYMSGAMQ0002: Bound messaging object to jndi name java:jboss/DefaultJMSConnectionFactory
14:20:12,185 INFO  [org.hibernate.validator.internal.util.Version] (MSC service thread 1-5) HV000001: Hibernate Validator 5.3.5.Final
14:20:12,256 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-5) WFLYEJB0473: JNDI bindings for session bean named 'SessionBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/SessionBean!service.SessionBean
	java:app/ejbtest/SessionBean!service.SessionBean
	java:module/SessionBean!service.SessionBean
	java:global/ejbtest/SessionBean!service.remote.ISession
	java:app/ejbtest/SessionBean!service.remote.ISession
	java:module/SessionBean!service.remote.ISession
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISession
	java:global/ejbtest/SessionBean!service.remote.ISessionHome
	java:app/ejbtest/SessionBean!service.remote.ISessionHome
	java:module/SessionBean!service.remote.ISessionHome
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISessionHome

14:20:12,257 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-5) WFLYEJB0473: JNDI bindings for session bean named 'ControllerBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/ControllerBean!service.ControllerBean
	java:app/ejbtest/ControllerBean!service.ControllerBean
	java:module/ControllerBean!service.ControllerBean
	java:global/ejbtest/ControllerBean
	java:app/ejbtest/ControllerBean
	java:module/ControllerBean

14:20:12,507 INFO  [org.jboss.weld.Version] (MSC service thread 1-5) WELD-000900: 2.4.2 (SP1)
14:20:12,935 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 71) WFLYCLINF0002: Started client-mappings cache from ejb container
14:20:13,549 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 69) WFLYUT0021: Registered web context: '/ejbtest' for server 'default-server'
14:20:13,570 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 40) WFLYSRV0010: Deployed "ejbtest.war" (runtime-name : "ejbtest.war")
14:20:13,678 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0212: Resuming server
14:20:13,680 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0060: Http management interface listening on http://127.0.0.1:9990/management
14:20:13,680 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0051: Admin console listening on http://127.0.0.1:9990
14:20:13,680 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: WildFly Core 3.0.0.Beta11 "Kenny" started in 4998ms - Started 594 of 818 services (449 services are lazy, passive or on-demand)
The batch executed successfully
process-state: reload-required 
{
    "outcome" => "success",
    "result" => undefined
}
14:20:14,083 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 69) WFLYMSGAMQ0006: Unbound messaging object to jndi name java:/ConnectionFactory
14:20:14,087 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 22) WFLYMSGAMQ0006: Unbound messaging object to jndi name java:jboss/exported/jms/RemoteConnectionFactory
14:20:14,089 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 4) WFLYUT0022: Unregistered web context: '/ejbtest' from server 'default-server'
14:20:14,104 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0019: Host default-host stopping
14:20:14,127 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-5) WFLYJCA0010: Unbound data source [java:jboss/datasources/ExampleDS]
14:20:14,129 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-3) WFLYMSGAMQ0006: Unbound messaging object to jndi name java:jboss/DefaultJMSConnectionFactory
14:20:14,129 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-3) WFLYJCA0011: Unbound JCA ConnectionFactory [java:/JmsXA]
14:20:14,133 INFO  [org.jboss.as.connector.deployers.jdbc] (MSC service thread 1-8) WFLYJCA0019: Stopped Driver service with driver-name = h2
14:20:14,137 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 69) WFLYCLINF0003: Stopped client-mappings cache from ejb container
14:20:14,143 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0008: Undertow HTTPS listener https suspending
14:20:14,143 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0007: Undertow HTTPS listener https stopped, was bound to 127.0.0.1:8443
14:20:14,144 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-2) WFLYSRV0028: Stopped deployment ejbtest.war (runtime-name: ejbtest.war) in 71ms
14:20:14,142 INFO  [org.apache.activemq.artemis.ra] (ServerService Thread Pool -- 30) AMQ151003: resource adaptor stopped
14:20:14,199 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 30) AMQ221002: Apache ActiveMQ Artemis Message Broker version 1.5.3.jbossorg-003 [d7881823-1604-11e7-89ce-e8b1fc470d10] stopped, uptime 3.021 seconds
14:20:14,199 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-6) WFLYUT0008: Undertow HTTP listener default suspending
14:20:14,199 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-6) WFLYUT0007: Undertow HTTP listener default stopped, was bound to 127.0.0.1:8080
14:20:14,200 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-6) WFLYUT0004: Undertow 1.4.11.Final stopping
14:20:14,206 INFO  [org.jboss.as.mail.extension] (MSC service thread 1-7) WFLYMAIL0002: Unbound mail session [java:jboss/mail/Default]
14:20:14,212 INFO  [org.jboss.as] (MSC service thread 1-8) WFLYSRV0050: WildFly Core 3.0.0.Beta11 "Kenny" stopped in 140ms
14:20:14,212 INFO  [org.jboss.as] (MSC service thread 1-8) WFLYSRV0049: WildFly Core 3.0.0.Beta11 "Kenny" starting
14:20:14,270 INFO  [org.jboss.as.controller.management-deprecated] (Controller Boot Thread) WFLYCTL0028: Attribute 'security-realm' in the resource at address '/core-service=management/management-interface=http-interface' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
14:20:14,288 INFO  [org.jboss.as.controller.management-deprecated] (ServerService Thread Pool -- 32) WFLYCTL0028: Attribute 'security-realm' in the resource at address '/subsystem=undertow/server=default-server/https-listener=https' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
14:20:14,324 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0039: Creating http management service using socket-binding (management-http)
14:20:14,334 INFO  [org.wildfly.extension.io] (ServerService Thread Pool -- 43) WFLYIO001: Worker 'default' has auto-configured to 16 core threads with 128 task threads based on your 8 available processors
14:20:14,337 INFO  [org.jboss.as.ejb3] (MSC service thread 1-8) WFLYEJB0481: Strict pool slsb-strict-max-pool is using a max instance size of 128 (per class), which is derived from thread worker pool sizing.
14:20:14,337 INFO  [org.jboss.as.ejb3] (MSC service thread 1-8) WFLYEJB0482: Strict pool mdb-strict-max-pool is using a max instance size of 32 (per class), which is derived from the number of CPUs on this host.
14:20:14,343 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 44) WFLYCLINF0001: Activating Infinispan subsystem.
14:20:14,347 INFO  [org.wildfly.iiop.openjdk] (ServerService Thread Pool -- 45) WFLYIIOP0001: Activating IIOP Subsystem
14:20:14,348 INFO  [org.jboss.as.jaxrs] (ServerService Thread Pool -- 46) WFLYRS0016: RESTEasy version 3.0.22.Final
14:20:14,348 INFO  [org.jboss.as.connector.subsystems.datasources] (ServerService Thread Pool -- 39) WFLYJCA0004: Deploying JDBC-compliant driver class org.h2.Driver (version 1.4)
14:20:14,349 INFO  [org.jboss.as.connector] (MSC service thread 1-7) WFLYJCA0009: Starting JCA Subsystem (WildFly/IronJacamar 1.4.2.Final)
14:20:14,350 INFO  [org.jboss.as.connector.deployers.jdbc] (MSC service thread 1-6) WFLYJCA0018: Started Driver service with driver-name = h2
14:20:14,355 INFO  [org.jboss.as.naming] (ServerService Thread Pool -- 55) WFLYNAM0001: Activating Naming Subsystem
14:20:14,357 INFO  [org.jboss.as.naming] (MSC service thread 1-4) WFLYNAM0003: Starting Naming Service
14:20:14,357 INFO  [org.jboss.as.mail.extension] (MSC service thread 1-3) WFLYMAIL0001: Bound mail session [java:jboss/mail/Default]
14:20:14,354 INFO  [org.wildfly.iiop.openjdk] (MSC service thread 1-1) WFLYIIOP0009: CORBA ORB Service started
14:20:14,365 WARN  [org.jboss.as.txn] (ServerService Thread Pool -- 64) WFLYTX0013: Node identifier property is set to the default value. Please make sure it is unique.
14:20:14,366 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-7) WFLYUT0003: Undertow 1.4.11.Final starting
14:20:14,368 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-7) WFLYUT0012: Started server default-server.
14:20:14,368 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 65) WFLYUT0014: Creating file handler for path '/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/welcome-content' with options [directory-listing: 'false', follow-symlink: 'false', case-sensitive: 'true', safe-symlink-paths: '[]']
14:20:14,372 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-1) WFLYUT0018: Host default-host starting
14:20:14,373 INFO  [org.jboss.as.security] (ServerService Thread Pool -- 63) WFLYSEC0002: Activating Security Subsystem
14:20:14,374 INFO  [org.jboss.as.security] (MSC service thread 1-1) WFLYSEC0001: Current PicketBox version=5.0.0.Beta1
14:20:14,375 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-7) WFLYUT0006: Undertow HTTP listener default listening on 127.0.0.1:8080
14:20:14,380 INFO  [org.jboss.as.webservices] (ServerService Thread Pool -- 66) WFLYWS0002: Activating WebServices Extension
14:20:14,416 INFO  [org.jboss.as.ejb3] (MSC service thread 1-2) WFLYEJB0493: EJB subsystem suspension complete
14:20:14,419 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-1) WFLYJCA0001: Bound data source [java:jboss/datasources/ExampleDS]
14:20:14,444 INFO  [org.jboss.as.patching] (MSC service thread 1-6) WFLYPAT0050: WildFly cumulative patch ID is: base, one-off patches include: none
14:20:14,446 WARN  [org.jboss.as.domain.management.security] (MSC service thread 1-4) WFLYDM0111: Keystore /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/configuration/application.keystore not found, it will be auto generated on first use with a self signed certificate for host localhost
14:20:14,447 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-4) WFLYSRV0027: Starting deployment of "ejbtest.war" (runtime-name: "ejbtest.war")
14:20:14,452 INFO  [org.jboss.as.server.deployment.scanner] (MSC service thread 1-4) WFLYDS0013: Started FileSystemDeploymentService for directory /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/deployments
14:20:14,462 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-3) WFLYUT0006: Undertow HTTPS listener https listening on 127.0.0.1:8443
14:20:14,463 INFO  [org.jboss.ws.common.management] (MSC service thread 1-3) JBWS022052: Starting JBossWS 5.1.8.Final (Apache CXF 3.1.10) 
14:20:14,497 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221000: live Message Broker is starting with configuration Broker Configuration (clustered=false,journalDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/journal,bindingsDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/bindings,largeMessagesDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/largemessages,pagingDirectory=/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/jboss-as/build/target/wildfly-11.0.0.Beta1-SNAPSHOT/standalone/data/activemq/paging)
14:20:14,498 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221012: Using AIO Journal
14:20:14,499 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-server]. Adding protocol support for: CORE
14:20:14,499 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-amqp-protocol]. Adding protocol support for: AMQP
14:20:14,499 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-hornetq-protocol]. Adding protocol support for: HORNETQ
14:20:14,499 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221043: Protocol module found: [artemis-stomp-protocol]. Adding protocol support for: STOMP
14:20:14,509 INFO  [org.jboss.weld.deployer] (MSC service thread 1-1) WFLYWELD0003: Processing weld deployment ejbtest.war
14:20:14,521 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-1) WFLYEJB0473: JNDI bindings for session bean named 'SessionBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/SessionBean!service.SessionBean
	java:app/ejbtest/SessionBean!service.SessionBean
	java:module/SessionBean!service.SessionBean
	java:global/ejbtest/SessionBean!service.remote.ISession
	java:app/ejbtest/SessionBean!service.remote.ISession
	java:module/SessionBean!service.remote.ISession
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISession
	java:global/ejbtest/SessionBean!service.remote.ISessionHome
	java:app/ejbtest/SessionBean!service.remote.ISessionHome
	java:module/SessionBean!service.remote.ISessionHome
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISessionHome

14:20:14,521 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-1) WFLYEJB0473: JNDI bindings for session bean named 'ControllerBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/ControllerBean!service.ControllerBean
	java:app/ejbtest/ControllerBean!service.ControllerBean
	java:module/ControllerBean!service.ControllerBean
	java:global/ejbtest/ControllerBean
	java:app/ejbtest/ControllerBean
	java:module/ControllerBean

14:20:14,592 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 69) WFLYCLINF0002: Started client-mappings cache from ejb container
14:20:14,740 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-3) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor acceptor
14:20:14,740 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-5) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor acceptor
14:20:14,740 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-7) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor-throughput acceptor
14:20:14,740 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-1) WFLYMSGAMQ0016: Registered HTTP upgrade for activemq-remoting protocol handled by http-acceptor-throughput acceptor
14:20:14,744 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221007: Server is now live
14:20:14,744 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 68) AMQ221001: Apache ActiveMQ Artemis Message Broker version 1.5.3.jbossorg-003 [default, nodeID=d7881823-1604-11e7-89ce-e8b1fc470d10] 
14:20:14,745 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 69) AMQ221003: Deploying queue jms.queue.DLQ
14:20:14,751 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-7) WFLYJCA0007: Registered connection factory java:/JmsXA
14:20:14,756 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 68) WFLYMSGAMQ0002: Bound messaging object to jndi name java:jboss/exported/jms/RemoteConnectionFactory
14:20:14,757 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 71) AMQ221003: Deploying queue jms.queue.ExpiryQueue
14:20:14,758 INFO  [org.apache.activemq.artemis.ra] (MSC service thread 1-7) Resource adaptor started
14:20:14,759 INFO  [org.jboss.as.connector.services.resourceadapters.ResourceAdapterActivatorService$ResourceAdapterActivator] (MSC service thread 1-7) IJ020002: Deployed: file://RaActivatoractivemq-ra
14:20:14,759 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 70) WFLYMSGAMQ0002: Bound messaging object to jndi name java:/ConnectionFactory
14:20:14,759 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-7) WFLYJCA0002: Bound JCA ConnectionFactory [java:/JmsXA]
14:20:14,759 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-1) WFLYMSGAMQ0002: Bound messaging object to jndi name java:jboss/DefaultJMSConnectionFactory
14:20:14,912 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 72) WFLYUT0021: Registered web context: '/ejbtest' for server 'default-server'
14:20:14,925 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 40) WFLYSRV0010: Deployed "ejbtest.war" (runtime-name : "ejbtest.war")
14:20:14,976 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0212: Resuming server
14:20:14,977 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0060: Http management interface listening on http://127.0.0.1:9990/management
14:20:14,977 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0051: Admin console listening on http://127.0.0.1:9990
14:20:14,977 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: WildFly Core 3.0.0.Beta11 "Kenny" started in 764ms - Started 594 of 818 services (449 services are lazy, passive or on-demand)
===== JTS interop quickstart: step 3 (start GlassFish):
/home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop
Waiting for domain1 to start ........
Successfully started the domain : domain1
domain  Location: /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/glassfish/appserver/distributions/glassfish/target/stage/glassfish4/glassfish/domains/domain1
Log File: /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/tmp/glassfish/appserver/distributions/glassfish/target/stage/glassfish4/glassfish/domains/domain1/logs/server.log
Admin Port: 4848
Command start-domain executed successfully.
configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port=7080
Command set executed successfully.
===== JTS interop quickstart: step 4 (deploy EJBs):
Application deployed with name ejbtest.
Command deploy executed successfully.
===== JTS interop quickstart: step 5 (EJB call gf -> wf):
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     014:20:32,754 INFO  [org.jboss.ejb.client] (p: default-threadpool; w: Idle) JBoss EJB Client version 4.0.0.Beta22
14:20:32,821 INFO  [stdout] (p: default-threadpool; w: Idle) enlisting dummy resource with fault type x
14:20:32,826 INFO  [stdout] (p: default-threadpool; w: Idle) DummyXAResource: start
14:20:32,841 INFO  [stdout] (p: default-threadpool; w: Idle) service.SessionBean@60b3022f returning next counter
14:20:32,882 INFO  [stdout] (p: default-threadpool; w: Idle) DummyXAResource: end
14:20:32,883 INFO  [stdout] (p: default-threadpool; w: Idle) DummyXAResource: prepare
14:20:32,968 INFO  [stdout] (p: default-threadpool; w: Idle) DummyXAResource: commit
100    10  100    10    0     0     16      0 --:--:-- --:--:-- --:--:--    16
Next: 800014:20:34,961 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 75) WFLYUT0022: Unregistered web context: '/ejbtest' from server 'default-server'
===== JTS interop quickstart: step 6 (EJB call wf -> gf):
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     014:20:34,999 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-1) WFLYSRV0028: Stopped deployment ejbtest.war (runtime-name: ejbtest.war) in 38ms
14:20:35,000 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 78) WFLYCLINF0003: Stopped client-mappings cache from ejb container
14:20:35,001 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-5) WFLYSRV0027: Starting deployment of "ejbtest.war" (runtime-name: "ejbtest.war")
100    74  100    74    0     0   5595      0 --:--:-- --:--:-- --:--:--  5692
<html><head><title>Error</title></head><body>404 - Not Found</body></html>14:20:35,031 INFO  [org.jboss.weld.deployer] (MSC service thread 1-3) WFLYWELD0003: Processing weld deployment ejbtest.war
14:20:35,037 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-3) WFLYEJB0473: JNDI bindings for session bean named 'SessionBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/SessionBean!service.SessionBean
	java:app/ejbtest/SessionBean!service.SessionBean
	java:module/SessionBean!service.SessionBean
	java:global/ejbtest/SessionBean!service.remote.ISession
	java:app/ejbtest/SessionBean!service.remote.ISession
	java:module/SessionBean!service.remote.ISession
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISession
	java:global/ejbtest/SessionBean!service.remote.ISessionHome
	java:app/ejbtest/SessionBean!service.remote.ISessionHome
	java:module/SessionBean!service.remote.ISessionHome
	java:jboss/exported/ejbtest/SessionBean!service.remote.ISessionHome

14:20:35,038 INFO  [org.jboss.as.ejb3.deployment] (MSC service thread 1-3) WFLYEJB0473: JNDI bindings for session bean named 'ControllerBean' in deployment unit 'deployment "ejbtest.war"' are as follows:

	java:global/ejbtest/ControllerBean!service.ControllerBean
	java:app/ejbtest/ControllerBean!service.ControllerBean
	java:module/ControllerBean!service.ControllerBean
	java:global/ejbtest/ControllerBean
	java:app/ejbtest/ControllerBean
	java:module/ControllerBean

14:20:35,093 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 78) WFLYCLINF0002: Started client-mappings cache from ejb container
14:20:35,222 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 78) WFLYUT0021: Registered web context: '/ejbtest' for server 'default-server'
14:20:35,233 INFO  [org.jboss.as.server] (DeploymentScanner-threads - 2) WFLYSRV0016: Replaced deployment "ejbtest.war" with deployment "ejbtest.war"
Waiting for the domain to stop .
Command stop-domain executed successfully.
14:20:38,731 INFO  [org.jboss.as.server] (management-handler-thread - 4) WFLYSRV0236: Suspending server with no timeout.
14:20:38,733 INFO  [org.jboss.as.ejb3] (management-handler-thread - 4) WFLYEJB0493: EJB subsystem suspension complete
14:20:38,734 INFO  [org.jboss.as.server] (Management Triggered Shutdown) WFLYSRV0241: Shutting down in response to management operation 'shutdown'
14:20:38,747 INFO  [org.wildfly.extension.messaging-activemq] (ServerService Thread Pool -- 78) WFLYMSGAMQ0006: Unbound messaging object to jndi name java:jboss/exported/jms/RemoteConnectionFactory
14:20:38,747 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 77) WFLYUT0022: Unregistered web context: '/ejbtest' from server 'default-server'
14:20:38,769 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-7) WFLYUT0019: Host default-host stopping
14:20:38,775 INFO  [org.wildfly.extension.messaging-activemq] (MSC service thread 1-1) WFLYMSGAMQ0006: Unbound messaging object to jndi name java:jboss/DefaultJMSConnectionFactory
14:20:38,775 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-1) WFLYJCA0010: Unbound data source [java:jboss/datasources/ExampleDS]
14:20:38,776 INFO  [org.jboss.as.connector.deployment] (MSC service thread 1-1) WFLYJCA0011: Unbound JCA ConnectionFactory [java:/JmsXA]
14:20:38,779 INFO  [org.jboss.as.connector.deployers.jdbc] (MSC service thread 1-1) WFLYJCA0019: Stopped Driver service with driver-name = h2
14:20:38,783 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 72) WFLYCLINF0003: Stopped client-mappings cache from ejb container
14:20:38,785 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0008: Undertow HTTPS listener https suspending
14:20:38,786 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-8) WFLYUT0007: Undertow HTTPS listener https stopped, was bound to 127.0.0.1:8443
14:20:38,786 INFO  [org.apache.activemq.artemis.ra] (ServerService Thread Pool -- 80) AMQ151003: resource adaptor stopped
14:20:38,787 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-5) WFLYSRV0028: Stopped deployment ejbtest.war (runtime-name: ejbtest.war) in 49ms
14:20:38,825 INFO  [org.apache.activemq.artemis.core.server] (ServerService Thread Pool -- 80) AMQ221002: Apache ActiveMQ Artemis Message Broker version 1.5.3.jbossorg-003 [d7881823-1604-11e7-89ce-e8b1fc470d10] stopped, uptime 24.328 seconds
14:20:38,826 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-3) WFLYUT0008: Undertow HTTP listener default suspending
14:20:38,826 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-3) WFLYUT0007: Undertow HTTP listener default stopped, was bound to 127.0.0.1:8080
14:20:38,827 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-6) WFLYUT0004: Undertow 1.4.11.Final stopping
14:20:38,829 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0050: WildFly Core 3.0.0.Beta11 "Kenny" stopped in 88ms
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ simple ---
[INFO] Installing /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/target/simple-5.5.6.Final-SNAPSHOT.jar to /home/mmusgrov/.m2/repository/org/jboss/narayana/quickstart/rts/simple/5.5.6.Final-SNAPSHOT/simple-5.5.6.Final-SNAPSHOT.jar
[INFO] Installing /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/interop/pom.xml to /home/mmusgrov/.m2/repository/org/jboss/narayana/quickstart/rts/simple/5.5.6.Final-SNAPSHOT/simple-5.5.6.Final-SNAPSHOT.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building JTS Examples 5.5.6.Final-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ narayana-quickstarts-jts ---
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ narayana-quickstarts-jts ---
[INFO] Installing /home/mmusgrov/work/source/forks/narayana/quickstart/ArjunaJTS/pom.xml to /home/mmusgrov/.m2/repository/org/jboss/narayana/quickstart/jts/narayana-quickstarts-jts/5.5.6.Final-SNAPSHOT/narayana-quickstarts-jts-5.5.6.Final-SNAPSHOT.pom
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building narayana-quickstarts-all 5.5.6.Final-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ narayana-quickstarts-all ---
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ narayana-quickstarts-all ---
[INFO] Installing /home/mmusgrov/work/source/forks/narayana/quickstart/pom.xml to /home/mmusgrov/.m2/repository/org/jboss/narayana/quickstart/jta/narayana-quickstarts-all/5.5.6.Final-SNAPSHOT/narayana-quickstarts-all-5.5.6.Final-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] JTS Interoperability Quickstart: GlassFish ......... SUCCESS [ 31.295 s]
[INFO] JTS Examples ....................................... SUCCESS [  0.004 s]
[INFO] narayana-quickstarts-all ........................... SUCCESS [  0.003 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 31.373 s
[INFO] Finished at: 2017-03-31T14:20:39+01:00
[INFO] Final Memory: 13M/298M
[INFO] ------------------------------------------------------------------------
Not a pull request, so not commenting

