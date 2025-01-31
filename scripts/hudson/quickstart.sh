#!/bin/bash

function fatal {
  comment_on_pull "Tests failed ($BUILD_URL): $1"
  echo "$1"; exit 1
}

set -x
[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then return; fi

    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

# Expects one argument as an integer number and adjust it
# with multiplication by MFACTOR value, if it's defined
function timeout_adjust {
  local re='^[0-9]+$'
  [[ "$1" =~ $re ]] || return 1 # is parameter defined and is it an int number?
  # is MFACTOR defined and is it an int number or is equal-or-less-or-equal than 1
  if ! [[ "$MFACTOR" =~ $re ]] || [ "$MFACTOR" -le 1 ]; then
    echo $1
    return 0
  fi
  echo $(($MFACTOR * $1))
}

function int_env {
  cd $WORKSPACE
  export GIT_ACCOUNT=jbosstm
  export GIT_REPO=quickstart
  export MFACTOR=${MFACTOR:-1}
  export -f timeout_adjust || echo "Function timeout_adjust won't be used in the subshells as it can't be exported"
  NARAYANA_REPO=${NARAYANA_REPO:-jbosstm}
  NARAYANA_BRANCH="${NARAYANA_BRANCH:-main}"
  QUICKSTART_NARAYANA_VERSION=${QUICKSTART_NARAYANA_VERSION:-7.2.2.Final-SNAPSHOT}
  REDUCE_SPACE=${REDUCE_SPACE:-0}

  [ $NARAYANA_CURRENT_VERSION ] || export NARAYANA_CURRENT_VERSION="7.2.2.Final-SNAPSHOT" 

  PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
  if [ "$PULL_NUMBER" != "" ]
  then
    PULL_DESCRIPTION=$(curl -H "Authorization: token $GITHUB_TOKEN" -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
    if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]; then
      echo "pull closed"
      exit 0
    fi
  fi
    _jdk=`which_java`
    if [ "$_jdk" -lt 17 ]; then
      fatal "Narayana does not support JDKs less than 17"
    fi
}
function which_java {
  type -p java 2>&1 > /dev/null
  if [ $? = 0 ]; then
    _java=java
  elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    _java="$JAVA_HOME/bin/java"
  else
    unset _java
  fi

  if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')

    if [[ $version = 17* ]]; then
      echo 17
    elif [[ $version = 11* ]]; then
      echo 11
    fi
  fi
}
function rebase_quickstart_repo {
  cd $WORKSPACE
  git remote add upstream https://github.com/jbosstm/quickstart.git
  export BRANCHPOINT=main
  git branch $BRANCHPOINT origin/$BRANCHPOINT
  git pull --rebase --ff-only origin $BRANCHPOINT
  if [ $? -ne 0 ]; then
    comment_on_pull "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually: $BUILD_URL"
    exit -1
  fi
}

function build_narayana {
  cd $WORKSPACE
  # INITIALIZE ENV
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"

  #rm -rf ~/.m2/repository/
  rm -rf narayana
  git clone https://github.com/${NARAYANA_REPO}/narayana.git -b ${NARAYANA_BRANCH}
  echo "Checking if need Narayana PR"
  if [ -n "$NY_BRANCH" ]; then
    echo "Building NY PR"
    [ $? = 0 ] || fatal "git clone https://github.com/${NARAYANA_REPO}/narayana.git failed"
    cd narayana
    git fetch origin +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
    [ $? = 0 ] || fatal "git fetch of pulls failed"
    git checkout $NY_BRANCH
    [ $? = 0 ] || fatal "git fetch of pull branch failed"
    cd ../
  fi
  
  if [ $? != 0 ]; then
    comment_on_pull "Checkout failed: $BUILD_URL";
    exit -1
  fi
  cd narayana
  ./build.sh clean install -B -DskipTests -Pcommunity

  if [ $? != 0 ]; then
    comment_on_pull "Narayana build failed: $BUILD_URL";
    exit -1
  fi
  if [ $REDUCE_SPACE = 1 ]; then
      echo "Deleting check out - assuming all artifacts are in the .m2"
      cd ..
      rm -rf narayana
  fi
}

function build_narayana_lra {
  cd $WORKSPACE
  # INITIALIZE LRA ENV
  export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"

  LRA_BRANCH=main
  #rm -rf ~/.m2/repository/
  rm -rf lra
  git clone https://github.com/${NARAYANA_REPO}/lra.git -b ${LRA_BRANCH}
  echo "Checking if need Narayana LRA PR"
  if [ -n "$NY_BRANCH" ]; then
    echo "Building NY PR"
    [ $? = 0 ] || fatal "git clone https://github.com/${NARAYANA_REPO}/lra.git failed"
    cd lra
    git fetch origin +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
    [ $? = 0 ] || fatal "git fetch of pulls failed"
    git checkout $NY_BRANCH
    [ $? = 0 ] || fatal "git fetch of pull branch failed"
    cd ../
  fi

  if [ $? != 0 ]; then
    comment_on_pull "Checkout failed: $BUILD_URL";
    exit -1
  fi
  cd lra
  ./build.sh clean install -B -DskipTests
  [ $LRA_CURRENT_VERSION ] || export LRA_CURRENT_VERSION=`grep "<version>" pom.xml | head -n 2 | tail -n 1 | sed "s/ *<version>//" | sed "s#</version>##"`
  cd ..

  if [ $? != 0 ]; then
    comment_on_pull "Narayana LRA build failed: $BUILD_URL";
    exit -1
  fi
}

function download_and_update_as {
  [ ! -z "${WILDFLY_RELEASE_VERSION}" ] || fatal "No WILDFLY_RELEASE_VERSION specified"
  
  cd $WORKSPACE
  
  # Check if the needed files are available in the m2 cache
  filesToCheck=(~/.m2/repository/org/jboss/narayana/rts/restat-api/${NARAYANA_CURRENT_VERSION}/restat-api-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/rts/restat-bridge/${NARAYANA_CURRENT_VERSION}/restat-bridge-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/rts/restat-integration/${NARAYANA_CURRENT_VERSION}/restat-integration-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/rts/restat-util/${NARAYANA_CURRENT_VERSION}/restat-util-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/xts/jbossxts/${NARAYANA_CURRENT_VERSION}/jbossxts-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/jbosstxbridge/${NARAYANA_CURRENT_VERSION}/jbosstxbridge-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/jts/narayana-jts-integration/${NARAYANA_CURRENT_VERSION}/narayana-jts-integration-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/jts/narayana-jts-idlj/${NARAYANA_CURRENT_VERSION}/narayana-jts-idlj-${NARAYANA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-service-base/${LRA_CURRENT_VERSION}/lra-service-base-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-service-base/${LRA_CURRENT_VERSION}/lra-service-base-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-service-base/${LRA_CURRENT_VERSION}/lra-service-base-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-coordinator-jar/${LRA_CURRENT_VERSION}/lra-coordinator-jar-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-client/${LRA_CURRENT_VERSION}/lra-client-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/narayana-lra/${LRA_CURRENT_VERSION}/narayana-lra-${LRA_CURRENT_VERSION}.jar ~/.m2/repository/org/jboss/narayana/lra/lra-proxy-api/${LRA_CURRENT_VERSION}/lra-proxy-api-${LRA_CURRENT_VERSION}.jar)
  goOffline=false
  for fileToCheck in "${filesToCheck[@]}"; do
    echo Checking $fileToCheck
    [ -f $fileToCheck ] || goOffline=true
  done  
  if [ $goOffline = true ]; then
      ./build.sh dependency:go-offline -DskipTests
  fi
  
  if [ ! -f wildfly-${WILDFLY_RELEASE_VERSION}.zip ]; then
    echo "Downloading AS"
    wget -N  https://github.com/wildfly/wildfly/releases/download/${WILDFLY_RELEASE_VERSION}/wildfly-${WILDFLY_RELEASE_VERSION}.zip
    [ $? -eq 0 ] || fatal "Could not download https://github.com/wildfly/wildfly/releases/download/${WILDFLY_RELEASE_VERSION}/wildfly-${WILDFLY_RELEASE_VERSION}.zip"
  fi
  rm -rf wildfly-${WILDFLY_RELEASE_VERSION}
  unzip wildfly-${WILDFLY_RELEASE_VERSION}.zip
  cp ~/.m2/repository/org/jboss/narayana/rts/restat-api/${NARAYANA_CURRENT_VERSION}/restat-api-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/rts/main/restat-api-*.jar
  [ $? -eq 0 ] || fatal "Could not copy restat-api-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/rts/restat-bridge/${NARAYANA_CURRENT_VERSION}/restat-bridge-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/rts/main/restat-bridge-*.jar
  [ $? -eq 0 ] || fatal "Could not copy restat-bridge-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/rts/restat-integration/${NARAYANA_CURRENT_VERSION}/restat-integration-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/rts/main/restat-integration-*.jar
  [ $? -eq 0 ] || fatal "Could not copy restat-integration-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/rts/restat-util/${NARAYANA_CURRENT_VERSION}/restat-util-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/rts/main/restat-util-*.jar
  [ $? -eq 0 ] || fatal "Could not copy restat-util-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/xts/jbossxts/${NARAYANA_CURRENT_VERSION}/jbossxts-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/xts/main/jbossxts-*.jar
  [ $? -eq 0 ] || fatal "Could not copy jbossxts-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/jbosstxbridge/${NARAYANA_CURRENT_VERSION}/jbosstxbridge-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/xts/main/jbosstxbridge-*.jar
  [ $? -eq 0 ] || fatal "Could not copy jbosstxbridge-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/jts/narayana-jts-integration/${NARAYANA_CURRENT_VERSION}/narayana-jts-integration-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/jts/integration/main/narayana-jts-integration-*.jar
  [ $? -eq 0 ] || fatal "Could not copy narayana-jts-integration-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/jts/narayana-jts-idlj/${NARAYANA_CURRENT_VERSION}/narayana-jts-idlj-${NARAYANA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/jts/main/narayana-jts-idlj-*.jar
  [ $? -eq 0 ] || fatal "Could not copy narayana-jts-idlj-${NARAYANA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/lra/lra-service-base/${LRA_CURRENT_VERSION}/lra-service-base-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-coordinator/main/lra-service-base-*.jar
  [ $? -eq 0 ] || fatal "Could not copy lra-service-base-${LRA_CURRENT_VERSION}.jar to lra-coordinator"
  cp ~/.m2/repository/org/jboss/narayana/lra/lra-service-base/${LRA_CURRENT_VERSION}/lra-service-base-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-participant/main/lra-service-base-*.jar
  [ $? -eq 0 ] || fatal "Could not copy lra-service-base-${LRA_CURRENT_VERSION}.jar to lra-participant"
  cp ~/.m2/repository/org/jboss/narayana/lra/lra-coordinator-jar/${LRA_CURRENT_VERSION}/lra-coordinator-jar-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-coordinator/main/lra-coordinator-jar-*.jar
  [ $? -eq 0 ] || fatal "Could not copy lra-coordinator.jar"
  cp ~/.m2/repository/org/jboss/narayana/lra/lra-client/${LRA_CURRENT_VERSION}/lra-client-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-participant/main/lra-client-*.jar
  [ $? -eq 0 ] || fatal "Could not copy lra-client-${LRA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/lra/narayana-lra/${LRA_CURRENT_VERSION}/narayana-lra-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-participant/main/narayana-lra-*.jar
  [ $? -eq 0 ] || fatal "Could not copy narayana-lra-${LRA_CURRENT_VERSION}.jar"
  cp ~/.m2/repository/org/jboss/narayana/lra/lra-proxy-api/${LRA_CURRENT_VERSION}/lra-proxy-api-${LRA_CURRENT_VERSION}.jar wildfly-${WILDFLY_RELEASE_VERSION}/modules/system/layers/base/org/jboss/narayana/lra/lra-participant/main/lra-proxy-api-*.jar
  [ $? -eq 0 ] || fatal "Could not copy lra-proxy-api-${LRA_CURRENT_VERSION}.jar"
  
  if [ $REDUCE_SPACE = 1 ]; then
    echo "Deleting wildfly-${WILDFLY_RELEASE_VERSION}.zip to reduce disk usage"
    rm wildfly-${WILDFLY_RELEASE_VERSION}.zip
  fi  
  
  export JBOSS_HOME=${WORKSPACE}/wildfly-${WILDFLY_RELEASE_VERSION}
  
  init_jboss_home

  cd $WORKSPACE
}

function init_jboss_home {
  [ -d $JBOSS_HOME ] || fatal "missing AS - $JBOSS_HOME is not a directory"
  echo "JBOSS_HOME=$JBOSS_HOME"
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-rts.xml ${JBOSS_HOME}/standalone/configuration
  # configuring bigger connection timeout for jboss cli (WFLY-13385)
  CONF="${JBOSS_HOME}/bin/jboss-cli.xml"
  sed -e 's#^\(.*</jboss-cli>\)#<connection-timeout>30000</connection-timeout>\n\1#' "$CONF" > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  grep 'connection-timeout' "${CONF}"
  #Enable remote debugger
  echo JAVA_OPTS='"$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8797,server=y,suspend=n"' >> "$JBOSS_HOME"/bin/standalone.conf
}
function run_quickstarts {
  cd $WORKSPACE
  echo Running quickstarts
  ./build.sh -B clean install -fae -DskipX11Tests=true -Dversion.narayana=$QUICKSTART_NARAYANA_VERSION -Dversion.org.jboss.narayana.lra=$LRA_CURRENT_VERSION

  if [ $? != 0 ]; then
    comment_on_pull "Pull failed: $BUILD_URL";
    exit -1
  else
    comment_on_pull "Pull passed: $BUILD_URL"
  fi
}

int_env
functionCalled=false
if [ $# -eq 1 ]; then
    if [ "$1" == "download_and_update_as" ]; then
        download_and_update_as
        functionCalled=true
    fi
fi
if [ $functionCalled = false ]; then
    comment_on_pull "Started testing this pull request: $BUILD_URL"
    rebase_quickstart_repo
    build_narayana
    build_narayana_lra
    if [ -z "$JBOSS_HOME" ]; then
      download_and_update_as "$@"
    fi
    run_quickstarts
fi
