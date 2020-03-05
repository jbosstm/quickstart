#!/bin/bash
source init.sh

set -e

# patch and build WildFly 
function build_wf {
  WS=$WORKSPACE

  cd "$QS_DIR/tmp/"
  WORKSPACE="$QS_DIR/tmp/wildfly/"

  rm -rf "$WORKSPACE"
  git clone --depth=1 https://github.com/wildfly/wildfly.git
  cd $WORKSPACE

  git apply $QS_DIR/interop.wildfly.diff

  ./build.sh clean install -B -DskipTests -Dversion.org.jboss.narayana=$NARAYANA_CURRENT_VERSION
  

  WILDFLY_MASTER_VERSION=`awk '/wildfly-parent/ { while(!/<version>/) {getline;} print; }' pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  cp -rp build/target/wildfly-$WILDFLY_MASTER_VERSION/ ${QS_DIR}/tmp/
  
  cd ${QS_DIR}
  rm -rf $WORKSPACE
  WORKSPACE=$WS 
}

# patch and build glassfish 
function build_gf {
  GLASSFISH=${GLASSFISH:=xyz}
  if [ ! -d $GLASSFISH ]; then
    echo looking for glassfish
    # if we are not running using jenkins CI then get the GlassFish sources via git clone
    if [ -z ${JENKINS_HOST+x} ]; then
      echo "getting glassfish via git clone; QS_DIR is at $QS_DIR"
      git clone --depth=1 https://github.com/javaee/glassfish -b 4.1.2 $QS_DIR/tmp/glassfish4
      cd $QS_DIR/tmp/glassfish4
      patch -p0 -i $QS_DIR/GLASSFISH-21532.diff  
      mvn install -B -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests  
      export GLASSFISH=$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4
      cd -
    fi
  fi

  [[ -f $GLASSFISH/bin/asadmin && -x $GLASSFISH/bin/asadmin ]] || fatal "asadmin not found"
}

rm -rf $QS_DIR/tmp
mkdir -p $QS_DIR/tmp

build_gf
build_wf
