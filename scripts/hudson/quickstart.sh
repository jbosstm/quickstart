#!/bin/bash

function fatal {
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
  QUICKSTART_NARAYANA_VERSION=${QUICKSTART_NARAYANA_VERSION:-7.0.2.Final-SNAPSHOT}
  REDUCE_SPACE=${REDUCE_SPACE:-0}

  [ $NARAYANA_CURRENT_VERSION ] || export NARAYANA_CURRENT_VERSION="7.0.2.Final-SNAPSHOT" 

  PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
  PULL_DESCRIPTION=$(curl -H "Authorization: token $GITHUB_TOKEN" -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
  if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]; then
    echo "pull closed"
    exit 0
  fi
  # WildFly 27 requires JDK 11 (see JBTM-3582 for details)
    _jdk=`which_java`
    if [ "$_jdk" -lt 11 ]; then
      fatal "Narayana does not support JDKs less than 11"
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
  echo "Deleting check out - assuming all artifacts are in the .m2"
  cd ..
  rm -rf narayana
}

function clone_as {
  echo "Cloning AS sources from https://github.com/jbosstm/jboss-as.git"

  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    rm -rf jboss-as # start afresh
  fi

  echo "First time checkout of WildFly"
  git clone https://github.com/jbosstm/jboss-as.git -o jbosstm
  [ $? -eq 0 ] || fatal "git clone https://github.com/jbosstm/jboss-as.git failed"

  cd jboss-as

  git remote add upstream https://github.com/wildfly/wildfly.git

  [ ! -z "$AS_BRANCH" ] || AS_BRANCH=main
  git checkout $AS_BRANCH
  [ $? -eq 0 ] || fatal "git checkout of branch $AS_BRANCH failed"

  git fetch upstream
  echo "This is the JBoss-AS commit"
  echo $(git rev-parse upstream/main)
  echo "This is the AS_BRANCH $AS_BRANCH commit"
  echo $(git rev-parse HEAD)

  echo "Rebasing the wildfly upstream/main on top of the AS_BRANCH $AS_BRANCH"
  git pull --rebase upstream main
  [ $? -eq 0 ] || fatal "git rebase failed"

  if [ $REDUCE_SPACE = 1 ]; then
    echo "Deleting git dir to reduce disk usage"
    rm -rf .git
  fi

  cd $WORKSPACE
}
function build_as {
  echo "Building WildFly's branch $AS_BRANCH"

  cd $WORKSPACE/jboss-as

  # building WildFly
  [ "$_jdk" -lt 17 ] && export MAVEN_OPTS="-XX:MaxMetaspaceSize=512m -XX:+UseConcMarkSweepGC $MAVEN_OPTS"
  [ "$_jdk" -ge 17 ] && export MAVEN_OPTS="-XX:MaxMetaspaceSize=512m $MAVEN_OPTS"
  JAVA_OPTS="-Xms1303m -Xmx1303m -XX:MaxMetaspaceSize=512m $JAVA_OPTS" ./build.sh clean install -B -DskipTests -Dts.smoke=false $IPV6_OPTS -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@"
  [ $? -eq 0 ] || fatal "AS build failed"

  WILDFLY_VERSION_FROM_JBOSS_AS=`awk '/wildfly-parent/ { while(!/<version>/) {getline;} print; }' ${WORKSPACE}/jboss-as/pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  echo "AS version is ${WILDFLY_VERSION_FROM_JBOSS_AS}"
  export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/wildfly-${WILDFLY_VERSION_FROM_JBOSS_AS}

  # init files under JBOSS_HOME before AS TESTS is started
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
  ./build.sh -B clean install -fae -DskipX11Tests=true -Dversion.narayana=$QUICKSTART_NARAYANA_VERSION

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
    if [ "$1" == "clone_as" ]; then
        clone_as
        functionCalled=true
    elif [ "$1" == "build_as" ]; then
        build_as
        functionCalled=true
    fi
fi
if [ $functionCalled = false ]; then
    comment_on_pull "Started testing this pull request: $BUILD_URL"
    rebase_quickstart_repo
    build_narayana
    if [ -z "$JBOSS_HOME" ]; then
      clone_as "$@"
      build_as "$@"
    fi
    run_quickstarts
fi
