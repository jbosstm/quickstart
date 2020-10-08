#!/bin/bash
set -x

function fatal {
  echo "$1"; exit 1
}

[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"
[ $NARAYANA_CURRENT_VERSION ] || fatal "please set NARAYANA_CURRENT_VERSION"

QS_DIR=${WORKSPACE}/ArjunaJTS/interop/glassfish
CLI_TIMEOUT_SECONDS=`timeout_adjust 60`

if ls ${QS_DIR}/tmp/wildfly-*/ 1> /dev/null 2>&1; then
  cd ${QS_DIR}/tmp/wildfly-*/
  export JBOSS_HOME=$(pwd)
  cd -
fi

if [ -d "/home/jenkins/glassfish4" ]; then
  GLASSFISH=/home/jenkins/glassfish4
elif [ -d "$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4" ]; then
  GLASSFISH=$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4
else
  unset GLASSFISH
fi

function waitForJBossToBeRunning() {
  [ "x$JBOSS_HOME" == "x" ] && echo "ERROR: Variable \$JBOSS_HOME is not defined." && return 1

  local jbossStateOutcome
  local jbossStateResult
  local jbossState
  local startTimeSeconds=$(date +"%s")
  local timeoutSeconds=${CLI_TIMEOUT_SECONDS:-10}

  # waiting for JBoss state when the jboss-cli.sh call was succesfull and the app server is running
  while [ "x${jbossStateOutcome}" != "xsuccess" ] && [ "x${jbossStateResult}" != "xrunning" ]; do

    # timeout verification
    if [ $((startTimeSeconds+timeoutSeconds)) -lt $(date +"%s") ]; then
      echo "ERROR: elapsed timeout when waiting on JBOSS at '$JBOSS_HOME' to be started"
      # kill jboss java process if available
      kill -9 $(jps | grep jboss-modules | cut -d" " -f1)
      exit 1
    fi

    # contacting JBoss via jboss-cli
    jbossState=$(${JBOSS_HOME}/bin/jboss-cli.sh -c --command=':read-attribute(name=server-state)') || true
    jbossStateOutcome=$(printf '%s' "$jbossState" | grep "outcome" | sed 's/.*"outcome"[^"]*"\([^"]*\).*/\1/')
    jbossStateResult=$(printf '%s' "$jbossState" | grep "result" | sed 's/.*"result"[^"]*"\([^"]*\).*/\1/')

    # state verification
    if [ "x${jbossStateOutcome}" != "xsuccess" ]; then
      echo "ERROR: cannot contact JBoss instance at '$JBOSS_HOME' with jboss-cli.sh command"
      echo $jbossState
      sleep 1
      continue
    fi
    if [ "x${jbossStateOutcome}" == "xsuccess" ]; then
      if [ "x${jbossStateResult}" == "xrestart-required" ]; then
        echo "JBoss at '$JBOSS_HOME' requires restart. Restarting..."
        ${JBOSS_HOME}/bin/jboss-cli.sh -c --command=':shutdown(restart=true)' || echo "Error on 'jboss-cli.sh' shutdown execution"
        continue
      fi
      if [ "x${jbossStateResult}" == "xreload-required" ]; then
        echo "JBoss at '$JBOSS_HOME' requires reload. Reloading..."
        ${JBOSS_HOME}/bin/jboss-cli.sh -c --command=':reload'  || echo "Error on 'jboss-cli.sh' on reload execution"
        break;
      fi
      if [ "x${jbossStateResult}" != "xrunning" ]; then
        echo "ERROR: cannot identify the state of the running JBOSS server at '$JBOSS_HOME'"
        echo $jbossState
        continue
      fi
    fi
  done
}

function executeJBossCli() {
  waitForJBossToBeRunning
  $JBOSS_HOME/bin/jboss-cli.sh --connect $@
  waitForJBossToBeRunning
}