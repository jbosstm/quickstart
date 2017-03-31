
# JBoss, Home of Professional Open Source
# Copyright 2016, Red Hat, Inc., and others contributors as indicated
# by the @authors tag. All rights reserved.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

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
}

function build_narayana {
  # INITIALIZE ENV
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

  [ $PROFILE ] || PROFILE=BLACKTIE 

  #rm -rf ~/.m2/repository/
  rm -rf narayana
  git clone https://github.com/${NARAYANA_REPO}/narayana.git
  if [ $? != 0 ]; then
    comment_on_pull "Checkout failed: $BUILD_URL";
    exit -1
  fi
  cd narayana
  WORKSPACE=$PWD COMMENT_ON_PULL="" PROFILE=$PROFILE ./scripts/hudson/narayana.sh -DskipTests -Pcommunity
  if [ $? != 0 ]; then
    comment_on_pull "Narayana build failed: $BUILD_URL";
    exit -1
  fi
}

function configure_wildfly {
  WILDFLY_MASTER_VERSION=10.1.0.Final
  
  rm -rf wildfly-$WILDFLY_MASTER_VERSION
  wget -N http://download.jboss.org/wildfly/$WILDFLY_MASTER_VERSION/wildfly-$WILDFLY_MASTER_VERSION.zip
  unzip wildfly-$WILDFLY_MASTER_VERSION.zip
  export JBOSS_HOME=$PWD/wildfly-$WILDFLY_MASTER_VERSION
  cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
  cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/
}

function build_apache-karaf {
  git clone https://github.com/apache/karaf.git apache-karaf
  if [ $? != 0 ]; then
    comment_on_pull "Karaf clone failed: $BUILD_URL";
    exit -1
  fi
  ./build.sh -f apache-karaf/pom.xml -Pfastinstall
  if [ $? != 0 ]; then
    comment_on_pull "Karaf build failed: $BUILD_URL";
    exit -1
  fi
}

function run_quickstarts {
  echo Running quickstarts
  BLACKTIE_DIST_HOME=$PWD/narayana/blacktie/blacktie/target/ ./build.sh clean install -DskipX11Tests=true

  if [ $? != 0 ]; then
    comment_on_pull "Pull failed: $BUILD_URL";
    exit -1
  else
    comment_on_pull "Pull passed: $BUILD_URL"
  fi
}

int_env
comment_on_pull "Started testing this pull request: $BUILD_URL"
rebase_quickstart_repo
get_bt_dependencies # TODO uncomment after testing
build_narayana
configure_wildfly
#build_apache-karaf # JBTM-2820 disable the karaf build
run_quickstarts
