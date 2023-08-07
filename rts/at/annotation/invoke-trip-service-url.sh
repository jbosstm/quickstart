#!/bin/bash

# URL to be invoked
URL="http://localhost:8082/trip/book?hotelName=Rex&flightNumber=123"

# Function to validate the response
validate_response() {
    local response="$1"

    # Check if the expected JSON string is present in the response
    if [ "$response" = "CONFIRMED" ]; then
        echo "Validation successful: Expected response found."
    else
        echo "Validation failed: Expected response not found."
    fi
}

# Invoke the URL using curl and capture the response
response=$(curl -XPOST "$URL")

echo $response

# Check if curl was successful
if [ $? -eq 0 ]; then
    echo "URL invoked successfully."
    # Validate the response


      # Extract the id value using jq
      id=$(echo "$response" | jq -r '.id')
      echo "Extracted id: $id"

       # Invoke the second URL using the extracted id
       if [ -n "$id" ]; then
       response_status=$(curl "http://localhost:8082/trip/status?sraId=$id")
       echo $response_status
       validate_response "$response_status"


        else
            echo "Failed to extract the id from the response."
       fi

else
    echo "Failed to invoke the URL. Please check if the URL is valid and accessible."
fi