#!/bin/bash
source init.sh

set -e

# build the latest version of narayana 
function build_narayana {
  WS=$WORKSPACE

  WORKSPACE="${QS_DIR}/tmp/narayana"

  rm -rf $WORKSPACE

  if [ ! -d "$WORKSPACE" ]; then
    git clone https://github.com/jbosstm/narayana.git $WORKSPACE
    [ $? = 0 ] || fatal "Git clone narayana repo failed"
  fi

  cd $WORKSPACE 

  PROFILE=NONE AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0 RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0 OSGI_TESTS=0 ./scripts/hudson/narayana.sh -B -DskipTests
  [ $? = 0 ] || fatal "Build narayana failed"

  WORKSPACE=$WS
}

# patch and build WildFly 
function build_wf {
  WS=$WORKSPACE

  WORKSPACE="$QS_DIR/tmp/narayana/jboss-as"

  cd $WORKSPACE

  git apply $QS_DIR/interop.wildfly.diff

  ./build.sh clean install -B -DskipTests -Drelease=true -Dlicense.skipDownloadLicenses=true -Dversion.org.jboss.narayana=$NARAYANA_CURRENT_VERSION
  WORKSPACE=$WS
}

# patch and build glassfish 
function build_gf {
  GLASSFISH=${GLASSFISH:=xyz}
  if [ ! -d $GLASSFISH ]; then
    echo looking for glassfish
    # if we are not running using jenkins CI then get the GlassFish sources via svn
    if [ -z ${JENKINS_HOST+x} ]; then
      echo getting glassfish via svn
      svn checkout https://svn.java.net/svn/glassfish~svn/trunk/main $QS_DIR/tmp/glassfish4
      cd $QS_DIR/tmp/glassfish4
      patch -p0 -i $QS_DIR/GLASSFISH-21532.diff  
      mvn install -B -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests  
      export GLASSFISH=$QS_DIR/tmp/glassfish4/appserver/distributions/glassfish/target/stage/glassfish4
    fi
  fi

  [[ -f $GLASSFISH/bin/asadmin && -x $GLASSFISH/bin/asadmin ]] || fatal "asadmin not found"
}

rm -rf $QS_DIR/tmp
mkdir -p $QS_DIR/tmp

build_gf
build_narayana
build_wf
