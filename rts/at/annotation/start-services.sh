#!/bin/bash

############################

# Step 1: Clean and package the Maven project, skipping tests
mvn clean package -DskipTests

# Step 2: Run flight-service in the background
java -jar flight-service/target/quarkus-app/quarkus-run.jar &

# Step 3: Run hotel-service in the background
java -jar hotel-service/target/quarkus-app/quarkus-run.jar &

# Step 4: Run trip-service in the background
java -jar trip-service/target/quarkus-app/quarkus-run.jar &

# Wait for a few seconds to give time for the services to start
sleep 10

# Check if the services are running
if $(jps | grep -q "quarkus-run.jar"); then
  echo "All services started successfully!"
else
  echo "Failed to start one or more services. Please check the logs for more information."
fi
