#!/bin/bash

# TODO I am waiting for https://github.com/jbosstm/narayana/pull/1157 to be merged
# after which the following will point to jbosstm and master
NARAYANA_REPO=mmusgrov
NARAYANA_BRANCH="JBTM-2623"

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then return; fi

    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -ujbosstm-bot:$BOT_PASSWORD https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

function int_env {
  export COMMENT_ON_PULL=1;
  export GIT_ACCOUNT=jbosstm
  export GIT_REPO=quickstart
  export MFACTOR=2 # double wait timeout period for crash recovery QA tests
  [ $NARAYANA_CURRENT_VERSION ] || export NARAYANA_CURRENT_VERSION="5.6.0.Final-SNAPSHOT"

  PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
  PULL_DESCRIPTION=$(curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
  if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]; then
    echo "pull closed"
    exit 0
  fi
}

function rebase_quickstart_repo {
  git remote add upstream https://github.com/jbosstm/quickstart.git
  export BRANCHPOINT=master
  git branch $BRANCHPOINT origin/$BRANCHPOINT
  git pull --rebase --ff-only origin $BRANCHPOINT
  if [ $? -ne 0 ]; then
    comment_on_pull "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually: $BUILD_URL"
  fi
}

function get_bt_dependencies {
  # SCP Blacktie thirdparty dependencies centos70x64
  set +e
  uname -a | grep el7 >> /dev/null
  if [ "$?" -ne "1" ]; then
  DEP=`find  ~/.m2 -name '*centos70x64*.md5'|grep -v blacktie|wc -l`
      if [ "$DEP" -ne "6" ]; then
      cd ~
      wget http://${JENKINS_HOST}/userContent/blacktie-thirdparty-centos70x64.tgz
      tar xzvf blacktie-thirdparty-centos70x64.tgz
      cd -
      fi
  fi
  set -e
}

function build_narayana {
  # INITIALIZE ENV
  export M2_HOME=/usr/local/apache-maven-3.0.4
  export ANT_HOME=/home/hudson/apache-ant-1.8.2
  export PATH=$M2_HOME/bin:$ANT_HOME/bin:$PATH
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

  [ $PROFILE ] || PROFILE=BLACKTIE 

  #rm -rf ~/.m2/repository/
  rm -rf narayana
  git clone https://github.com/${NARAYANA_REPO}/narayana.git
  cd narayana
  git checkout $NARAYANA_BRANCH
  WORKSPACE=$PWD PROFILE=$PROFILE ./scripts/hudson/narayana.sh -DskipTests -Pcommunity
  cd jboss-as
  WILDFLY_MASTER_VERSION=`awk "/wildfly-parent/ {getline;print;}" pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  cd ..
  cd ..
}

function build_apache-karaf {
  rm -rf wildfly-$WILDFLY_MASTER_VERSION
  cp -rp narayana/jboss-as/build/target/wildfly-${WILDFLY_MASTER_VERSION}/ .
  export JBOSS_HOME=$PWD/wildfly-$WILDFLY_MASTER_VERSION
  cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
  cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/

  rm -rf apache-karaf
  git clone https://github.com/apache/karaf.git apache-karaf
  cd apache-karaf
  mvn -Pfastinstall
  cd ..
}

function run_quickstarts {
  echo Running quickstarts
  set +e
  BLACKTIE_DIST_HOME=$PWD/narayana/blacktie/blacktie/target/ mvn clean install -DskipX11Tests=true

  if [ $? != 0 ]; then
    comment_on_pull "Pull failed: $BUILD_URL";
    exit -1
  else
    comment_on_pull "Pull passed: $BUILD_URL"
  fi
  set -e
}

int_env
#comment_on_pull "Started testing this pull request: $BUILD_URL"
#rebase_quickstart_repo
#get_bt_dependencies # TODO uncomment after testing
#build_narayana
#build_apache-karaf # TODO uncomment after testing
run_quickstarts # TODO uncomment after testing
