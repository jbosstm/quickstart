#!/bin/bash

# Change the following variables to match your WildFly installation
#JBOSS_HOME="/home/mshikalw/Documents/AllGitFork/wildfly/build/target/wildfly-28.0.2.Final-SNAPSHOT"
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


echo "Starting all services"

# Call and run start-services.sh
source ./start-services.sh


echo "Invoking trip service"

# Call and run invoke-trip-service-url.sh
source ./invoke-trip-service-url.sh


echo "Stopping wildfly server"

# Call and run scrstop-wildfly-coordinatoript.sh
source stop-wildfly-coordinator.sh


echo "Stopping all services"

# Call and run stop-services.sh using the 'source' or '.' command
source ./stop-services.sh
