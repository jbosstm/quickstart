#!/bin/bash

# URL to be invoked
URL="http://localhost:8082/trip/book?hotelName=Rex&flightNumber=123"

# Function to validate the response
validate_response() {
    local response="$1"
    local expected_json='{"cancelPending":false'

    # Check if the expected JSON string is present in the response
    if echo "$response" | grep -qF "$expected_json"; then
        echo "Validation successful: Expected JSON found in the response."
    else
        echo "Validation failed: Expected JSON not found in the response."
    fi
}

# Invoke the URL using curl and capture the response
response=$(curl -XPOST "$URL")

echo $response

# Check if curl was successful
if [ $? -eq 0 ]; then
    echo "URL invoked successfully."
    # Validate the response
    validate_response "$response"
else
    echo "Failed to invoke the URL. Please check if the URL is valid and accessible."
fi