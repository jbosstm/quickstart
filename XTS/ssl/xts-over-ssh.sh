#!/bin/sh
set -x

# Script for quickstart execution of XTS over HTTPS, see
# https://github.com/jbosstm/quickstart/tree/master/XTS/ssl
# the original XTS quickstart could be found at
# https://github.com/wildfly/quickstart.git

#--------------------- VARIABLES SETUP ------------------
# Script works with port offsets
# client is started with offset 100 (-Djboss.socket.binding.port-offset=100)
# server is started with offset 200 (-Djboss.socket.binding.port-offset=200)
CLIENT_OFFSET=100
SERVER_OFFSET=200
CLI_PORT_BASE=9990
HTTPS_PORT_BASE=8443
CLIENT_CLI_PORT=$((CLI_PORT_BASE+CLIENT_OFFSET))
SERVER_CLI_PORT=$((CLI_PORT_BASE+SERVER_OFFSET))
CLIENT_HTTPS_PORT=$((CLIENT_OFFSET+HTTPS_PORT_BASE))
SERVER_HTTPS_PORT=$((SERVER_OFFSET+HTTPS_PORT_BASE))
SLEEP_TIME=3

WORKSPACE=${WORKSPACE:-$PWD}
QUICKSTART_HOME=${QUICKSTART_HOME:-$WORKSPACE/quickstart}
WSAT_QUICKSTART_PATH="${QUICKSTART_HOME}/wsat-simple"
JBOSS_BIN=${JBOSS_BIN:-$JBOSS_HOME} # jboss_bin is unpacked jboss distribution
JBOSS_CLIENT="${WORKSPACE}/client"
JBOSS_SERVER="${WORKSPACE}/server"
# we don't want jboss home settings confusing startup scripts
unset JBOSS_HOME

function waitServerStarted() {
  local PORT=${1:-$CLI_PORT_BASE}
  local RETURN_CODE=-1
  local TIMEOUT=20
  local TIMESTAMP_START=`date +%s`
  local NOT_TIMEOUTED=true
  while [ $RETURN_CODE -ne 0 ] && $NOT_TIMEOUTED; do
    "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$PORT --command=":read-attribute(name=server-state)" | grep -s running
    RETURN_CODE=$?
    [ $((`date +%s`-TIMESTAMP_START)) -gt ${TIMEOUT} ] && NOT_TIMEOUTED=false
  done
  if $NOT_TIMEOUTED; then
    sleep $SLEEP_TIME
  else
    echo "Timeout ${TIMEOUT}s exceeded when waiting for container at port $PORT"
  fi
}

function prepareServerWorkingDirectories() {
  local JBOSS_HOME_BASE_DIR="${1}"
  local JBOSS_TARGET_DIR="${2}"
  mkdir -p "${JBOSS_TARGET_DIR}/standalone/configuration"
  mkdir -p "${JBOSS_TARGET_DIR}/standalone/data"
  mkdir -p "${JBOSS_TARGET_DIR}/standalone/tmp"
  mkdir -p "${JBOSS_TARGET_DIR}/standalone/log"
  mkdir -p "${JBOSS_TARGET_DIR}/standalone/content"
  cp "$JBOSS_HOME_BASE_DIR"/standalone/configuration/*.properties "${JBOSS_TARGET_DIR}/standalone/configuration"
}

function getWflyStartupParameters() {
  local PORT_OFFSET=$1
  local JBOSS_CONF_DIR="$2"
  echo "-c standalone-xts.xml -Djboss.socket.binding.port-offset=$PORT_OFFSET -Djboss.server.data.dir=${JBOSS_CONF_DIR}/standalone/data -Djboss.server.log.dir=${JBOSS_CONF_DIR}/standalone/log -Djboss.server.temp.dir=${JBOSS_CONF_DIR}/standalone/tmp -Djboss.server.deploy.dir=${JBOSS_CONF_DIR}/standalone/content -Djboss.server.config.dir=${JBOSS_CONF_DIR}/standalone/configuration"
}


#--------------------- WORKSPACE PREPARATION ------------------
cd "$WORKSPACE"

if [ ! -d "$JBOSS_BIN" ]; then
  echo "Variable \$JBOSS_BIN not defined going to clone 'wildfly' from github"
  git clone --depth=1 https://github.com/wildfly/wildfly.git
  mvn clean install -DskipTests -f wildfly/pom.xml
  JBOSS_BIN=`find wildfly/dist/target -maxdepth 1 -type d -name 'wildfly-*'`
fi
echo "Creating jboss distro directories '$JBOSS_CLIENT' and '$JBOSS_SERVER'"
rm -rf "$JBOSS_CLIENT"
rm -rf "$JBOSS_SERVER"
prepareServerWorkingDirectories "$JBOSS_BIN" "$JBOSS_CLIENT"
prepareServerWorkingDirectories "$JBOSS_BIN" "$JBOSS_SERVER"

if [ ! -d "$QUICKSTART_HOME" ]; then
  echo "Variable \$QUICKSTART_HOME not defined going to clone 'wfly quickstart' from github"
  git clone --depth=1 https://github.com/wildfly/quickstart.git
fi

if [ ! -d "$WSAT_QUICKSTART_PATH" ]; then
  echo "Quickstart WSAT directory "$WSAT_QUICKSTART_PATH" does not exist"
  exit 1
fi


#--------------------- CONTAINERS CONFIGURATION ------------------
echo "Going to configure quickstart '$WSAT_QUICKSTART_PATH' to use SSL"
cd $WSAT_QUICKSTART_PATH

# to client knows how to connect to https server endpoint
sed "s#http://localhost:8080/jboss-as-wsat-simple/RestaurantServiceAT#https://localhost:${SERVER_HTTPS_PORT}/jboss-as-wsat-simple/RestaurantServiceAT#"\
 -i ./src/main/webapp/WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl
sed "s#http://localhost:8080/wsat-simple/RestaurantServiceAT?wsdl#https://localhost:${SERVER_HTTPS_PORT}/wsat-simple/RestaurantServiceAT?wsdl#"\
 -i ./src/test/java/org/jboss/as/quickstarts/wsat/simple/Client.java

patch -p0 <<EOF
diff --git pom.xml pom.xml
index 97c5693..5fea194 100644
--- pom.xml
+++ pom.xml
@@ -92,5 +92,19 @@
     <build>
         <\!-- Set the name of the WAR, used as the context root when the app is deployed. -->
         <finalName>\${project.artifactId}</finalName>
+        <plugins>
+            <plugin>
+                <groupId>org.apache.maven.plugins</groupId>
+                <artifactId>maven-war-plugin</artifactId>
+                <configuration>
+                    <archive>
+                        <manifest/>
+                        <manifestEntries>
+                          <Dependencies>org.jboss.xts</Dependencies>
+                        </manifestEntries>
+                    </archive>
+                </configuration>
+            </plugin>
+        </plugins>
     </build>
 </project>
EOF

# -s ../settings.xml
mvn clean install -B -Dversion.server.bom=14.0.1.Final
DEPLOYMENT_NAME=wsat-simple.war

# Settings for client
echo "Going to configure jboss client '$JBOSS_CLIENT' to use SSL"
cd "$JBOSS_CLIENT"

cp "$JBOSS_BIN/docs/examples/configs/standalone-xts.xml" standalone/configuration/
keytool -genkey -alias client -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass client -storepass client -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
keytool -export -keystore ./standalone/configuration/server.keystore -alias client -file client.cer -keypass client -storepass client

JBOSS_CLIENT_STARTUP_PARAMS=`getWflyStartupParameters $CLIENT_OFFSET "$JBOSS_CLIENT"`
"$JBOSS_BIN"/bin/standalone.sh $JBOSS_CLIENT_STARTUP_PARAMS &
CLIENT_PID=$!
waitServerStarted $CLIENT_CLI_PORT
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands='
/core-service=management/security-realm=SSLRealm:add(),
/core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=${jboss.server.config.dir}/server.keystore, keystore-password=client, alias=client),
/subsystem=undertow/server=default-server/https-listener=https:remove()'
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands=':reload()'
waitServerStarted $CLIENT_CLI_PORT
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands='
/subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")'
echo "Deploying quickstart '${WSAT_QUICKSTART_PATH}/target/wsat-simple.war' at $JBOSS_CLIENT"
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --command="deploy ${WSAT_QUICKSTART_PATH}/target/$DEPLOYMENT_NAME"
pkill -9 -P $CLIENT_PID
sleep $SLEEP_TIME

sed "s#\(xts-environment.*:\)8080/#\1${CLIENT_HTTPS_PORT}/#"\
 -i standalone/configuration/standalone-xts.xml
sed "s#http:#https:#"\
 -i standalone/configuration/standalone-xts.xml


# Settings for server
echo "Going to configure jboss server '$JBOSS_SERVER' to use SSL"
cd "$JBOSS_SERVER"

cp "$JBOSS_BIN/docs/examples/configs/standalone-xts.xml" standalone/configuration/
keytool -genkey -alias server -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass server -storepass server -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
keytool -export -keystore ./standalone/configuration/server.keystore -alias server -file server.cer -keypass server -storepass server

JBOSS_SERVER_STARTUP_PARAMS=`getWflyStartupParameters $SERVER_OFFSET "$JBOSS_SERVER"`
"$JBOSS_BIN"/bin/standalone.sh $JBOSS_SERVER_STARTUP_PARAMS &
SERVER_PID=$!
waitServerStarted $SERVER_CLI_PORT
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands='
/core-service=management/security-realm=SSLRealm:add(),
/core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=${jboss.server.config.dir}/server.keystore, keystore-password=server, alias=server),
/subsystem=undertow/server=default-server/https-listener=https:remove()'
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands=':reload()'
waitServerStarted $SERVER_CLI_PORT
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands='
/subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")'
echo "Deploying quickstart '${WSAT_QUICKSTART_PATH}/target/wsat-simple.war' at $JBOSS_SERVER"
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --command="deploy ${WSAT_QUICKSTART_PATH}/target/$DEPLOYMENT_NAME"
pkill -9 -P $SERVER_PID
sleep $SLEEP_TIME

sed "s#\(xts-environment.*:\)8080/#\1${SERVER_HTTPS_PORT}/#"\
 -i standalone/configuration/standalone-xts.xml
sed "s#http:#https:#"\
 -i standalone/configuration/standalone-xts.xml

# Importing ssl keys
cd "$JBOSS_CLIENT"
keytool -import -noprompt -v -trustcacerts -alias server -file "$JBOSS_SERVER/server.cer" -keystore ./standalone/configuration/server.keystore -storepass client
cd "$JBOSS_SERVER"
keytool -import -noprompt -v -trustcacerts -alias client -file "$JBOSS_CLIENT/client.cer" -keystore ./standalone/configuration/server.keystore -storepass server


#--------------------- DEPLOY AND RUN ------------------
cd "$WORKSPACE"
echo "Starting servers: server at ${JBOSS_SERVER}"
${JBOSS_BIN}/bin/standalone.sh $JBOSS_SERVER_STARTUP_PARAMS -Djavax.net.ssl.trustStore="${JBOSS_SERVER}/standalone/configuration/server.keystore" -Djavax.net.ssl.trustStorePassword=server -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore="${JBOSS_SERVER}/standalone/configuration/server.keystore" -Djavax.net.ssl.keyStorePassword=server -Dcxf.tls-client.disableCNCheck=true &
SERVER_PID=$!
waitServerStarted $SERVER_CLI_PORT
echo "Starting servers: client at ${JBOSS_CLIENT}"
${JBOSS_BIN}/bin/standalone.sh  $JBOSS_CLIENT_STARTUP_PARAMS -Djavax.net.ssl.trustStore="${JBOSS_CLIENT}/standalone/configuration/server.keystore" -Djavax.net.ssl.trustStorePassword=client -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore="${JBOSS_CLIENT}/standalone/configuration/server.keystore" -Djavax.net.ssl.keyStorePassword=client -Dcxf.tls-client.disableCNCheck=true &
CLIENT_PID=$!
waitServerStarted $CLIENT_CLI_PORT

# Do the test
CLIENT_HTTP_PORT=$((CLIENT_OFFSET + 8080))
echo "Doing the test by requesting 'http://localhost:${CLIENT_HTTP_PORT}/WSATSimpleServletClient'"
curl -X GET "http://localhost:${CLIENT_HTTP_PORT}/WSATSimpleServletClient" | grep 'Transaction succeeded!'
SUCCESS=$?

pkill -9 -P $CLIENT_PID
pkill -9 -P $SERVER_PID
sleep $SLEEP_TIME

rm -rf "$JBOSS_CLIENT"
rm -rf "$JBOSS_SERVER"

# if not successed (grep returns 0 on sucess) then fail the script
[ $SUCCESS -eq 0 ] && exit 0 || exit 1

