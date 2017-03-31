#!/bin/bash
set -ex

function fatal {
  echo "$1"; exit 1
}

[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"
[ $NARAYANA_CURRENT_VERSION ] || fatal "please set NARAYANA_CURRENT_VERSION"

QS_DIR=${WORKSPACE}/ArjunaJTS/interop/glassfish
JBOSS_SRC=${QS_DIR}/tmp/narayana/jboss-as
if [ -f "${JBOSS_SRC}/pom.xml" ]; then
  WILDFLY_MASTER_VERSION=`awk "/wildfly-parent/ {getline;print;}" ${JBOSS_SRC}/pom.xml | cut -d \< -f 2|cut -d \> -f 2`
fi

export JBOSS_HOME="$JBOSS_SRC/build/target/wildfly-$WILDFLY_MASTER_VERSION"

if [ -d "/home/jenkins/glassfish4" ]; then
  GLASSFISH=/home/jenkins/glassfish4
elif [ -d "$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4" ]; then
  GLASSFISH=$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4
else
  unset GLASSFISH
fi
