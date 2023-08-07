#!/bin/bash

# Stop WildFly
echo "Stopping WildFly..."
#JBOSS_HOME="/home/mshikalw/Documents/AllGitFork/wildfly/build/target/wildfly-28.0.2.Final-SNAPSHOT"
WILDFLY_BIN=$JBOSS_HOME/bin

$WILDFLY_BIN/jboss-cli.sh --connect command=:shutdown

# Wait for a few seconds to give time for the server to stop
sleep 10

# Check if WildFly is still running
if $(jps | grep -q "jboss-modules"); then
  echo "Failed to stop WildFly. Please check the logs for more information."
else
  echo "WildFly stopped successfully!"
fi
