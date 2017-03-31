#!/bin/bash
set -e

function fatal {
  echo "$1"; exit 1
}

[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"
[ $NARAYANA_CURRENT_VERSION ] || fatal "please set NARAYANA_CURRENT_VERSION"

QS_DIR=${WORKSPACE}/ArjunaJTS/interop
if [ -f ${QS_DIR}/tmp/jboss-as/pom.xml ]; then
  WILDFLY_MASTER_VERSION=`awk "/wildfly-parent/ {getline;print;}" ${QS_DIR}/tmp/jboss-as/pom.xml | cut -d \< -f 2|cut -d \> -f 2`
fi

export JBOSS_HOME=$QS_DIR/tmp/jboss-as/build/target/wildfly-$WILDFLY_MASTER_VERSION
export PATH=$QS_DIR/tmp/glassfish/appserver/distributions/glassfish/target/stage/glassfish4/bin:$PATH

