#!/bin/bash

# Start WildFly
echo "Starting WildFly..."
#JBOSS_HOME="/home/mshikalw/Documents/AllGitFork/wildfly/build/target/wildfly-28.0.2.Final-SNAPSHOT"
WILDFLY_BIN=$JBOSS_HOME/bin

$WILDFLY_BIN/standalone.sh -c standalone-rts.xml > /dev/null 2>&1 &

# Wait for a few seconds to give time for the server to start
sleep 10

# Check if WildFly is running
if $(jps | grep -q "jboss-modules"); then
  echo "WildFly started successfully!"
else
  echo "Failed to start WildFly. Please check the logs for more information."
fi