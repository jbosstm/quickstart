
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

function fatal {
  echo "$1"; exit 1
}

[ $WORKSPACE ] || fatal "please set WORKSPACE to the quickstarts directory"
NARAYANA_REPO=${NARAYANA_REPO:-jbosstm}
NARAYANA_BRANCH="${NARAYANA_BRANCH:-master}"
QUICKSTART_NARAYANA_VERSION=${QUICKSTART_NARAYANA_VERSION:-5.10.5.Final-SNAPSHOT}

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
  cd $WORKSPACE
  export GIT_ACCOUNT=jbosstm
  export GIT_REPO=quickstart
  export MFACTOR=2 # double wait timeout period for crash recovery QA tests

  [ $NARAYANA_CURRENT_VERSION ] || export NARAYANA_CURRENT_VERSION="5.10.5.Final-SNAPSHOT" 

  PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
  PULL_DESCRIPTION=$(curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
  if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]; then
    echo "pull closed"
    exit 0
  fi
}

function rebase_quickstart_repo {
  cd $WORKSPACE
  git remote add upstream https://github.com/jbosstm/quickstart.git
  export BRANCHPOINT=master
  git branch $BRANCHPOINT origin/$BRANCHPOINT
  git pull --rebase --ff-only origin $BRANCHPOINT
  if [ $? -ne 0 ]; then
    comment_on_pull "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually: $BUILD_URL"
  fi
}

function get_bt_dependencies {
  cd $WORKSPACE
  # SCP Blacktie thirdparty dependencies centos70x64
  set +e
  uname -a | grep el7 >> /dev/null
  if [ "$?" -ne "1" ]; then
  DEP=`find  ~/.m2 -name '*centos70x64*.md5'|grep -v blacktie|wc -l`
      if [ "$DEP" -ne "6" ]; then
      wget http://${JENKINS_HOST}/userContent/blacktie-thirdparty-centos70x64.tgz
      tar xzvf blacktie-thirdparty-centos70x64.tgz
      rm blacktie-thirdparty-centos70x64.tgz
      fi
  fi
}

function build_narayana {
  cd $WORKSPACE
  # INITIALIZE ENV
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

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
  ./build.sh -f blacktie/wildfly-blacktie/pom.xml clean install -B
  ./build.sh -f blacktie/pom.xml clean install -B -DskipTests
  
  if [ $? != 0 ]; then
    comment_on_pull "Narayana build failed: $BUILD_URL";
    exit -1
  fi
  echo "Deleting check out - assuming all artifacts are in the .m2"
  cp -rp narayana-full/target/narayana-full-5.10.5.Final-SNAPSHOT-bin.zip $WORKSPACE
  cd ..
  rm -rf narayana
}

function configure_wildfly {
  cd $WORKSPACE
  wget --user=guest --password=guest -nv https://ci.wildfly.org/httpAuth/repository/downloadAll/WF_Nightly/.lastSuccessful/artifacts.zip
  unzip -q artifacts.zip
  export WILDFLY_DIST_ZIP=$(ls wildfly-*-SNAPSHOT.zip)
  unzip -q $WILDFLY_DIST_ZIP
  export JBOSS_HOME=`pwd`/${WILDFLY_DIST_ZIP%.zip}
  cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
  cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/
  # cleaning
  rm -f artifacts.zip
  rm -rf "${WILDFLY_DIST_ZIP}"
}

function build_apache-karaf {
  cd $WORKSPACE
  git clone --depth=1 https://github.com/apache/karaf.git apache-karaf
  if [ $? != 0 ]; then
    comment_on_pull "Karaf clone failed: $BUILD_URL";
    exit -1
  fi
  ./build.sh -f apache-karaf/pom.xml -B -Pfastinstall
  if [ $? != 0 ]; then
    comment_on_pull "Karaf build failed: $BUILD_URL";
    exit -1
  fi
}

function run_quickstarts {
  cd $WORKSPACE
  echo Running quickstarts
  BLACKTIE_DIST_HOME=$PWD/narayana/blacktie/blacktie/target/ ./build.sh -B clean install -DskipX11Tests=true -Dversion.narayana=$QUICKSTART_NARAYANA_VERSION

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
#get_bt_dependencies # JBTM-2878 missing userContent on JENKINS_HOST
build_narayana
configure_wildfly
#build_apache-karaf # JBTM-2820 disable the karaf build
run_quickstarts
