#!/bin/bash

# Function to stop a service by its process name
stop_service() {
    local service_name="$1"
    pkill -f "$service_name"
}

# Stop the flight-service
stop_service "flight-service"

# Stop the hotel-service
stop_service "hotel-service"

# Stop the trip-service
stop_service "trip-service"

# Wait for a few seconds to give time for the services to stop
sleep 5

# Check if the services are still running
if $(jps | grep -q "quarkus-run.jar"); then
    echo "Failed to stop one or more services. Please check the logs for more information."
else
    echo "All services stopped successfully!"
fi