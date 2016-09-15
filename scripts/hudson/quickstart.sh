#!/bin/bash

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


export GIT_ACCOUNT=jbosstm
export GIT_REPO=quickstart
export MFACTOR=2 # double wait timeout period for crash recovery QA tests

PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
PULL_DESCRIPTION=$(curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]; then
  echo "pull closed"
  exit 0
fi


comment_on_pull "Started testing this pull request: $BUILD_URL"

git remote add upstream https://github.com/jbosstm/quickstart.git
export BRANCHPOINT=master
git branch $BRANCHPOINT origin/$BRANCHPOINT
git pull --rebase --ff-only origin $BRANCHPOINT
if [ $? -ne 0 ]; then
  comment_on_pull "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually: $BUILD_URL"
fi


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

# INITIALIZE ENV
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

#rm -rf ~/.m2/repository/
rm -rf narayana
git clone https://github.com/jbosstm/narayana.git
if [ $? != 0 ]; then
  comment_on_pull "Checkout failed: $BUILD_URL";
  exit -1
fi
cd narayana
WORKSPACE=$PWD COMMENT_ON_PULL="" PROFILE=BLACKTIE ./scripts/hudson/narayana.sh -DskipTests -Pcommunity
if [ $? != 0 ]; then
  comment_on_pull "Narayana build failed: $BUILD_URL";
  exit -1
fi
cd jboss-as
WILDFLY_MASTER_VERSION=`awk "/wildfly-parent/ {getline;print;}" pom.xml | cut -d \< -f 2|cut -d \> -f 2`
if [ $? != 0 ]; then
  comment_on_pull "WildFly version check failed: $BUILD_URL";
  exit -1
fi
cd ..
cd ..

rm -rf wildfly-$WILDFLY_MASTER_VERSION
cp -rp narayana/jboss-as/build/target/wildfly-${WILDFLY_MASTER_VERSION}/ .
export JBOSS_HOME=$PWD/wildfly-$WILDFLY_MASTER_VERSION
cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/

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

echo Running quickstarts
BLACKTIE_DIST_HOME=$PWD/narayana/blacktie/blacktie/target/ ./build.sh clean install -DskipX11Tests=true

if [ $? != 0 ]; then
  comment_on_pull "Pull failed: $BUILD_URL";
  exit -1
else
  comment_on_pull "Pull passed: $BUILD_URL"
fi