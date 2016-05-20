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


export COMMENT_ON_PULL=1;
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
export BRANCHPOINT=5.2
git branch $BRANCHPOINT origin/$BRANCHPOINT
git pull --rebase --ff-only origin $BRANCHPOINT
if [ $? -ne 0 ]; then
  comment_on_pull "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually: $BUILD_URL"
fi


# INITIALIZE ENV
export M2_HOME=/usr/local/apache-maven-3.0.4
export ANT_HOME=/home/hudson/apache-ant-1.8.2
export PATH=$M2_HOME/bin:$ANT_HOME/bin:$PATH
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

#rm -rf ~/.m2/repository/
rm -rf narayana
git clone https://github.com/jbosstm/narayana.git
cd narayana
git checkout $BRANCHPOINT
WORKSPACE=$PWD PROFILE=MAIN ./scripts/hudson/narayana.sh -DskipTests -Pcommunity
cd ..

wget --no-check-certificate https://ci.jboss.org/hudson/job/WildFly-latest-master/lastBuild/artifact/dist/target/wildfly-10.x.zip
rm -rf wildfly-10.*.*.Final-SNAPSHOT/
unzip wildfly-10.x.zip
cd $PWD/wildfly-10.*.*.Final-SNAPSHOT/ && export JBOSS_HOME=$PWD && cd ..
cp $JBOSS_HOME/docs/examples/configs/standalone-xts.xml $JBOSS_HOME/standalone/configuration/
cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/

git clone https://github.com/apache/karaf.git apache-karaf
cd apache-karaf
mvn -Pfastinstall
cd ..

echo Running quickstarts
set +e
mvn clean install -DskipX11Tests=true

if [ $? != 0 ]; then
  comment_on_pull "Pull failed: $BUILD_URL";
  exit -1
else
  comment_on_pull "Pull passed: $BUILD_URL"
fi
set -e