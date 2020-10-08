#!/bin/bash
source init.sh
set -x
set +e

# undeploy ejbs and stop GlassFish
export PATH=$GLASSFISH/bin:$PATH
asadmin --port 4848 undeploy ejbtest
asadmin stop-domain domain1

# undeploy ejbs and stop WildFly
[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"
waitForJBossToBeRunning
${JBOSS_HOME}/bin/jboss-cli.sh --connect --command=':shutdown'
