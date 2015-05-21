wildfly: setCheckedActionFactoryExample
=======================================
Author: Tom Jenkinson

Introduction
------------

This quickstart shows how you can use checked actions inside the WildFly container


Running the Quickstart
----------------------

You will need a WildFly server

    mvn clean install
    cp target/ROOT.war <WFLY_HOME>/standalone/deployments
    <WFLY_HOME>/bin/standalone.sh

The output will be similar to:

    INFO  [org.jboss.weld.deployer] (MSC service thread 1-1) WFLYWELD0003: Processing weld deployment ROOT.war
    INFO  [org.hibernate.validator.internal.util.Version] (MSC service thread 1-1) HV000001: Hibernate Validator 5.1.3.Final
    INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-1) JNDI bindings for session bean named MyEJB in deployment unit deployment "ROOT.war" are as follows:

            java:global/ROOT/MyEJB!org.narayana.handler.MyEJB
            java:app/ROOT/MyEJB!org.narayana.handler.MyEJB
            java:module/MyEJB!org.narayana.handler.MyEJB
            java:global/ROOT/MyEJB
            java:app/ROOT/MyEJB
            java:module/MyEJB

    INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-1) JNDI bindings for session bean named StartupBean in deployment unit deployment "ROOT.war" are as follows:

            java:global/ROOT/StartupBean!org.narayana.StartupBean
            java:app/ROOT/StartupBean!org.narayana.StartupBean
            java:module/StartupBean!org.narayana.StartupBean
            java:global/ROOT/StartupBean
            java:app/ROOT/StartupBean
            java:module/StartupBean

    INFO  [org.jboss.weld.deployer] (MSC service thread 1-15) WFLYWELD0006: Starting Services for CDI deployment: ROOT.war
    INFO  [org.jboss.weld.Version] (MSC service thread 1-15) WELD-000900: 2.2.11 (Final)
    INFO  [org.jboss.weld.deployer] (MSC service thread 1-16) WFLYWELD0009: Starting weld service for deployment ROOT.war
    INFO  [stdout] (ServerService Thread Pool -- 58) StartupBean call
    INFO  [stdout] (ServerService Thread Pool -- 58) MyEJB call
    WARN  [com.arjuna.ats.arjuna] (ServerService Thread Pool -- 58) ARJUNA012094: Commit of action id 0:ffff0a247431:17c6d5d6:555990b1:10 invoked while multiple threads active within it.
    WARN  [com.arjuna.ats.arjuna] (ServerService Thread Pool -- 58) ARJUNA012107: CheckedAction::check - atomic action 0:ffff0a247431:17c6d5d6:555990b1:10 commiting with 1 threads active!

Understanding the Code
----------------------

To understand the code and the checked action API, it is recommended that you:

1. Browse the code reading the comments
2. Read the following document: http://narayana.io/docs/product/index.html#d0e2528