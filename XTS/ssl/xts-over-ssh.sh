#!/bin/bash
set -x

trap cleanup EXIT

# Script for quickstart execution of XTS over HTTPS, see
# https://github.com/jbosstm/quickstart/tree/master/XTS/ssl
# the original XTS quickstart could be found at
# https://github.com/wildfly/quickstart.git

#--------------------- VARIABLES SETUP ------------------
# Script works with port offsets
# client is started with offset 100 (-Djboss.socket.binding.port-offset=100)
# server is started with offset 200 (-Djboss.socket.binding.port-offset=200)

elytron=1 # WildFly 25+ uses elytron
interactive=0 # set to non-zero to pause for input at key milestones

CLIENT_OFFSET=100
SERVER_OFFSET=200
CLI_PORT_BASE=9990
HTTPS_PORT_BASE=8443
CLIENT_CLI_PORT=$((CLI_PORT_BASE+CLIENT_OFFSET))
SERVER_CLI_PORT=$((CLI_PORT_BASE+SERVER_OFFSET))
CLIENT_HTTPS_PORT=$((CLIENT_OFFSET+HTTPS_PORT_BASE))
SERVER_HTTPS_PORT=$((SERVER_OFFSET+HTTPS_PORT_BASE))
SLEEP_TIME=`timeout_adjust 3 2>/dev/null || echo 3` # default sleep time is 3

WORKSPACE=${WORKSPACE:-$PWD}
WILDFLY_QUICKSTART_HOME=${WILDFLY_QUICKSTART_HOME:-$QUICKSTART_HOME}
JBOSS_BIN=${JBOSS_BIN:-$JBOSS_HOME} # jboss_bin is unpacked jboss distribution
JBOSS_CLIENT="${WORKSPACE}/client"
JBOSS_SERVER="${WORKSPACE}/server"
# we don't want jboss home settings confusing startup scripts
unset JBOSS_HOME

CURRENT_SCRIPT_ABSPATH=`readlink -f "$0"`
CURRENT_SCRIPT_DIR=`dirname "$ABSPATH"`

function cleanup {
  [ -z ${CLIENT_PID+x} ] || pkill -9 -P $CLIENT_PID
  [ -z ${SERVER_PID+x} ] || pkill -9 -P $SERVER_PID
  unset CLIENT_PID SERVER_PID
  sleep $SLEEP_TIME

  rm -rf "$JBOSS_CLIENT"
  rm -rf "$JBOSS_SERVER"
}

function prompt {
  if [ "$interactive" -ne "0" ]; then
    read -p "$1: continue? (y/n): " -s -n 1 confirm
    if [ "$confirm" = "n" ]; then
      exit 1
    fi
  else
    echo "$1"
  fi
}

function verify {
  if [ $1 -ne 0 ]; then
    echo "last command failed"
    exit $1
  fi
}

function cli_command {
  ${JBOSS_BIN}/bin/jboss-cli.sh -c --controller=localhost:${1} --commands=${2}
  verify $?
}

function verifyIsUp {
  ${JBOSS_BIN}/bin/jboss-cli.sh -c --controller=localhost:$PORT --command=":read-attribute(name=server-state)" | grep -s running
}

# configure_elytron port secret
function configure_elytron {
  cli_command $1 "/subsystem=elytron/key-store=LocalhostKeyStore:add(path=server.keystore,relative-to=jboss.server.config.dir,credential-reference={clear-text=\"$2\"},type=JKS)"
  cli_command $1 "/subsystem=elytron/key-manager=LocalhostKeyManager:add(key-store=LocalhostKeyStore,alias-filter=\"$2\",credential-reference={clear-text=\"$2\"})"
  cli_command $1 "/subsystem=elytron/server-ssl-context=LocalhostSslContext:add(key-manager=LocalhostKeyManager)"
  cli_command $1 "/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)"
  cli_command $1 "/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context,value=LocalhostSslContext)"

  cli_command $1 ":reload()"
}

function waitServerStarted() {
  local PORT=${1:-$CLI_PORT_BASE}
  local TIMEOUT=`timeout_adjust 40 2>/dev/null || echo 40` # default timeout is 40 seconds
  local secs=0

  until [ $secs -gt $TIMEOUT ]; do
    "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$PORT --command=":read-attribute(name=server-state)" | grep -s running
    if [ $? = 0 ]; then return 0; fi
    ((secs++))
  done

  echo "Timeout ${TIMEOUT}s exceeded when waiting for container at port $PORT"
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$PORT --command=":shutdown"
  exit 2
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
  echo "-c standalone-xts.xml -Djboss.socket.binding.port-offset=$PORT_OFFSET -Djboss.server.data.dir=${JBOSS_CONF_DIR}/standalone/data -Djboss.server.log.dir=${JBOSS_CONF_DIR}/standalone/log -Djboss.server.temp.dir=${JBOSS_CONF_DIR}/standalone/tmp -Djboss.server.deploy.dir=${JBOSS_CONF_DIR}/standalone/content -Djboss.server.config.dir=${JBOSS_CONF_DIR}/standalone/configuration" # -Djavax.net.debug=all"
}

function patchWildFlyParent {
  patchfile=$(mktemp)

  # workaround for CI failure because of https://github.com/jboss/jboss-parent-pom/issues/65
  cat << EOF > $td
@@ -119,6 +119,16 @@
                     </archive>
                 </configuration>
             </plugin>
+            <plugin>
+              <groupId>org.apache.maven.plugins</groupId>
+              <artifactId>maven-enforcer-plugin</artifactId>
+              <executions>
+                <execution>
+                  <id>enforce-java-version</id>
+                  <phase>none</phase>
+                </execution>
+              </executions>
+            </plugin>
         </plugins>
     </build>
 </project>
EOF
  if ! patch -R -p0 -s -f --dry-run <patchfile; then
    patch -p0 --ignore-whitespace "$WSAT_QUICKSTART_PATH/pom.xml" < patchfile
  fi

  rm -f patchfile
}

#--------------------- WORKSPACE PREPARATION ------------------
cd "$WORKSPACE"

if [ ! -d "$JBOSS_BIN" ]; then
  [ -d "$CURRENT_SCRIPT_DIR/target" ] && cd "$CURRENT_SCRIPT_DIR/target"
  echo "Variable \$JBOSS_BIN not defined going to clone 'wildfly' from github"
  git clone --depth=1 https://github.com/wildfly/wildfly.git
  mvn clean install -DskipTests -f wildfly/pom.xml

  JBOSS_BIN=`find wildfly/dist/target -maxdepth 1 -type d -name 'wildfly-*'`
  command -v realpath
  [ $? -eq 0 ] && JBOSS_BIN=`realpath "$JBOSS_BIN"`

  [ ! -d "$JBOSS_BIN" ] && echo "WildFly cloned and build at '$CURRENT_SCRIPT_DIR/target/wildfly but the absolute path to directory '$JBOSS_BIN' with the built sources does not exist." && exit 3
  [ -d "$CURRENT_SCRIPT_DIR/target" ] && cd -
fi

echo "Creating jboss distro directories '$JBOSS_CLIENT' and '$JBOSS_SERVER'"
rm -rf "$JBOSS_CLIENT" "$JBOSS_SERVER"
prepareServerWorkingDirectories "$JBOSS_BIN" "$JBOSS_CLIENT"
prepareServerWorkingDirectories "$JBOSS_BIN" "$JBOSS_SERVER"

if [ ! -d "$WILDFLY_QUICKSTART_HOME" ]; then
  [ -d "$CURRENT_SCRIPT_DIR/target" ] && cd "$CURRENT_SCRIPT_DIR/target"
  echo "Variable \$WILDFLY_QUICKSTART_HOME not defined going to clone 'wildfly quickstarts' from github.com/wildfly/quickstart"
  git clone --depth=1 https://github.com/wildfly/quickstart.git wildfly-quickstart

  WILDFLY_QUICKSTART_HOME="${PWD}/wildfly-quickstart"
  [ -d "$CURRENT_SCRIPT_DIR/target" ] && cd -
fi

WSAT_QUICKSTART_PATH="${WILDFLY_QUICKSTART_HOME}/wsat-simple"
if [ ! -d "$WSAT_QUICKSTART_PATH" ]; then
  echo "Quickstart WSAT directory "$WSAT_QUICKSTART_PATH" does not exist"
  exit 1
fi

patchWildFlyParent

#--------------------- CONTAINERS CONFIGURATION ------------------
prompt "Going to configure quickstart '$WSAT_QUICKSTART_PATH' to use SSL"
cd $WSAT_QUICKSTART_PATH

# to client knows how to connect to https server endpoint
sed "s#http://localhost:8080/jboss-as-wsat-simple/RestaurantServiceAT#https://localhost:${SERVER_HTTPS_PORT}/jboss-as-wsat-simple/RestaurantServiceAT#"\
 -i ./src/main/webapp/WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl
sed "s#http://localhost:8080/wsat-simple/RestaurantServiceAT?wsdl#https://localhost:${SERVER_HTTPS_PORT}/wsat-simple/RestaurantServiceAT?wsdl#"\
 -i ./src/test/java/org/jboss/as/quickstarts/wsat/simple/Client.java

# -s ../settings.xml
DEPLOYMENT_NAME=wsat-simple.war
mvn clean install -B -e -Dversion.server.bom=20.0.0.Final -Dversion.microprofile.bom=20.0.0.Final -DskipTests -Dinsecure.repositories=WARN
[ $? -ne 0 ] && echo "Failure to build deployment '$DEPLOYMENT_NAME' from quickstart '$WSAT_QUICKSTART_PATH'"

# Settings for client
prompt "Going to configure jboss client '$JBOSS_CLIENT' to use SSL"

cd "$JBOSS_CLIENT"

cp "$JBOSS_BIN/docs/examples/configs/standalone-xts.xml" standalone/configuration/
keytool -genkey -alias client -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass client -storepass client -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
keytool -export -keystore ./standalone/configuration/server.keystore -alias client -file client.cer -keypass client -storepass client
verify $?

JBOSS_CLIENT_STARTUP_PARAMS=`getWflyStartupParameters $CLIENT_OFFSET "$JBOSS_CLIENT"`
"$JBOSS_BIN"/bin/standalone.sh $JBOSS_CLIENT_STARTUP_PARAMS & #> ${JBOSS_CLIENT}/client.log 2>&1  &
CLIENT_PID=$!
waitServerStarted $CLIENT_CLI_PORT
if [ $elytron ]; then
  prompt "client running - configuring ssl using elytron"

  configure_elytron $CLIENT_CLI_PORT client
else
  prompt "client running - configuring ssl"
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands='
/core-service=management/security-realm=SSLRealm:add(),
/core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=${jboss.server.config.dir}/server.keystore, keystore-password=client, alias=client),
/subsystem=undertow/server=default-server/https-listener=https:remove()'

  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands=':reload()'
fi

waitServerStarted $CLIENT_CLI_PORT

if [ ! $elytron ]; then
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --commands='
/subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")'
fi
echo "Deploying quickstart '${WSAT_QUICKSTART_PATH}/target/wsat-simple.war' at $JBOSS_CLIENT"
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$CLIENT_CLI_PORT --command="deploy ${WSAT_QUICKSTART_PATH}/target/$DEPLOYMENT_NAME"
WAS_CLIENT_DEPLOYED=$?
verify $WAS_CLIENT_DEPLOYED
echo "client configured - killing"
pkill -9 -P $CLIENT_PID
sleep $SLEEP_TIME
[ $WAS_CLIENT_DEPLOYED -ne 0 ] && echo "Failed to deploy $DEPLOYMENT_NAME to 'localhost:$CLIENT_CLI_PORT'" && exit 1

sed "s#\(xts-environment.*:\)8080/#\1${CLIENT_HTTPS_PORT}/#"\
 -i standalone/configuration/standalone-xts.xml
sed "s#http:#https:#"\
 -i standalone/configuration/standalone-xts.xml

# Settings for server
prompt "Going to configure jboss server '$JBOSS_SERVER' to use SSL"
cd "$JBOSS_SERVER"

prompt "server configuring keystore"
cp "$JBOSS_BIN/docs/examples/configs/standalone-xts.xml" standalone/configuration/
keytool -genkey -alias server -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass server -storepass server -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
keytool -export -keystore ./standalone/configuration/server.keystore -alias server -file server.cer -keypass server -storepass server

prompt "server starting"
JBOSS_SERVER_STARTUP_PARAMS=`getWflyStartupParameters $SERVER_OFFSET "$JBOSS_SERVER"`
"$JBOSS_BIN"/bin/standalone.sh $JBOSS_SERVER_STARTUP_PARAMS & #> ${JBOSS_SERVER}/server.log 2>&1  &
SERVER_PID=$!
waitServerStarted $SERVER_CLI_PORT

if [ $elytron ]; then
  configure_elytron $SERVER_CLI_PORT server
else
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands='
/core-service=management/security-realm=SSLRealm:add(),
/core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=${jboss.server.config.dir}/server.keystore, keystore-password=server, alias=server),
/subsystem=undertow/server=default-server/https-listener=https:remove()'
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands=':reload()'
fi

waitServerStarted $SERVER_CLI_PORT
if [ $elytron -ne 1 ]; then
  "$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --commands='
/subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")'
fi

echo "Deploying quickstart '${WSAT_QUICKSTART_PATH}/target/wsat-simple.war' at $JBOSS_SERVER"
"$JBOSS_BIN"/bin/jboss-cli.sh -c --controller=localhost:$SERVER_CLI_PORT --command="deploy ${WSAT_QUICKSTART_PATH}/target/$DEPLOYMENT_NAME"
WAS_SERVER_DEPLOYED=$?
pkill -9 -P $SERVER_PID
sleep $SLEEP_TIME
[ $WAS_SERVER_DEPLOYED -ne 0 ] && echo "Failed to deploy $DEPLOYMENT_NAME to 'localhost:$SERVER_CLI_PORT'" && exit 1

prompt "server configuring ports and ssl keys"
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
prompt "Starting servers: server at ${JBOSS_SERVER}"
${JBOSS_BIN}/bin/standalone.sh $JBOSS_SERVER_STARTUP_PARAMS -Djavax.net.ssl.trustStore="${JBOSS_SERVER}/standalone/configuration/server.keystore" -Djavax.net.ssl.trustStorePassword=server -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore="${JBOSS_SERVER}/standalone/configuration/server.keystore" -Djavax.net.ssl.keyStorePassword=server -Dcxf.tls-client.disableCNCheck=true & #> ${JBOSS_SERVER}/restart-server.log 2>&1 &
SERVER_PID=$!
waitServerStarted $SERVER_CLI_PORT
echo "Starting servers: client at ${JBOSS_CLIENT}"
prompt "Starting servers: client at ${JBOSS_CLIENT}"
${JBOSS_BIN}/bin/standalone.sh  $JBOSS_CLIENT_STARTUP_PARAMS -Djavax.net.ssl.trustStore="${JBOSS_CLIENT}/standalone/configuration/server.keystore" -Djavax.net.ssl.trustStorePassword=client -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore="${JBOSS_CLIENT}/standalone/configuration/server.keystore" -Djavax.net.ssl.keyStorePassword=client -Dcxf.tls-client.disableCNCheck=true & #> ${JBOSS_CLIENT}/restart-client.log 2>&1 &
CLIENT_PID=$!
waitServerStarted $CLIENT_CLI_PORT

prompt "running the test"
# Do the test
CLIENT_HTTP_PORT=$((CLIENT_OFFSET + 8080))
echo "Doing the test by requesting 'http://localhost:${CLIENT_HTTP_PORT}/WSATSimpleServletClient'"
curl -X GET "http://localhost:${CLIENT_HTTP_PORT}/WSATSimpleServletClient" | grep 'Transaction succeeded!'
SUCCESS=$?
prompt "test result: $SUCCESS"
[ $SUCCESS -eq 0 ] && exit 0 || exit 1
