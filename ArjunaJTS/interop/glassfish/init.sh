#!/bin/bash
set -x

function fatal {
  echo "$1"; exit 1
}

[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"
[ $NARAYANA_CURRENT_VERSION ] || fatal "please set NARAYANA_CURRENT_VERSION"

QS_DIR=${WORKSPACE}/ArjunaJTS/interop/glassfish

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
