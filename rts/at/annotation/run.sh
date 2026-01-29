#!/bin/bash

trap finish EXIT

function finish() {
   for pid in "$ID1" "$ID2" "$ID3"; do
       [[ -z "$pid" || ! "$pid" =~ ^[0-9]+$ ]] && continue

       if kill -0 "$pid" 2>/dev/null; then
           kill -9 "$pid" 2>/dev/null || true
       fi
   done
}

# Change the following variables to match your WildFly installation

WILDFLY_BIN="$JBOSS_HOME/bin"

# Source file path (replace with the path of the file you want to copy)
SOURCE_FILE="$JBOSS_HOME/docs/examples/configs/standalone-rts.xml"

# Destination directory path (replace with the path of the directory where you want to copy the file)
DESTINATION_DIR="$JBOSS_HOME/standalone/configuration/"

# Check if the source file exists
if [ -f "$SOURCE_FILE" ]; then
    # Check if the destination directory exists
    if [ -d "$DESTINATION_DIR" ]; then
        # Use 'cp' command to copy the file
        cp "$SOURCE_FILE" "$DESTINATION_DIR"
        echo "File copied successfully to $DESTINATION_DIR"
    else
        echo "Destination directory $DESTINATION_DIR does not exist."
    fi
else
    echo "Source file $SOURCE_FILE does not exist."
fi

## Start Wilfly server

echo "Starting wildfly server"

# Call and run start-wildfly-coordinator.sh
source ./start-wildfly-coordinator.sh

################################
####### Start services #########
################################

echo "Starting all services"

mvn clean package -DskipTests

java -jar flight-service/target/quarkus-app/quarkus-run.jar &
ID1=$!
java -jar hotel-service/target/quarkus-app/quarkus-run.jar &
ID2=$!
java -jar trip-service/target/quarkus-app/quarkus-run.jar &
ID3=$!

# Wait for a few seconds to give time for the services to start
sleep 10

# Check if the services are running
if $(jps | grep -q "quarkus-run.jar"); then
  echo "All services started successfully!"
else
  echo "Failed to start one or more services. Please check the logs for more information."
fi

################################
############# END ##############
################################


echo "Invoking trip service"

# Call and run invoke-trip-service-url.sh
source ./invoke-trip-service-url.sh


echo "Stopping wildfly server"

# Call and run scrstop-wildfly-coordinatoript.sh
source stop-wildfly-coordinator.sh
