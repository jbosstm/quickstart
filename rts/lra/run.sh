#!/usr/bin/env bash
set -e
set -o pipefail
set -x
trap finish EXIT

function finish() {
  echo "Killing quarkus processes"
   for pid in "$ID1" "$ID2" "$ID3" "$ID4" "$ID5"; do
       [[ -z "$pid" || ! "$pid" =~ ^[0-9]+$ ]] && continue

       if kill -0 "$pid" 2>/dev/null; then
           kill -9 "$pid" 2>/dev/null || true
       fi
   done
}

urlencode() {
    # urlencode <string>
    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C

    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done

    LC_COLLATE=$old_lc_collate
}

function wait_for_recovery() {
    coord_port=8080
    echo " ===== waiting for recovery ......."
    curl ${CURL_IP_OPTS} http://localhost:${coord_port}/lra-coordinator/recovery
    # sometimes it can take two scans to complete recovery
    curl ${CURL_IP_OPTS} http://localhost:${coord_port}/lra-coordinator/recovery
    echo " ===== recovery should have happened"
}

function wait_for_all_quarkus_apps() {
    set +e
    echo "===== Waiting for quarkus applications to be ready..."

    QUARKUS_APPS_PORTS=(8080 8081 8082 8083 8084)

    for i in {1..60}; do
        CHECK=0

        for port in "${QUARKUS_APPS_PORTS[@]}"; do
            if curl -s "http://localhost:${port}/q/health/ready" >/dev/null; then
                echo "Quarkus application at ${port} ready"
                ((CHECK++))
            else
                echo "Quarkus application at ${port} NOT ready"
            fi
        done

        if (( CHECK == ${#QUARKUS_APPS_PORTS[@]} )); then
            echo "All quarkus application are ready"
            break
        fi

        sleep 2
    done
    set -e
}

function getDebugArgs {
    [ $DEBUG ] && echo "$JDWP"$1 || echo ""
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE=$(cd "$SCRIPT_DIR/../.." && pwd)
echo "WORKSPACE is set to: ${WORKSPACE}"

if [ ! -f $WORKSPACE/rts/lra-examples/coordinator-quarkus/target/lra-coordinator-quarkus-runner.jar ]; then
    mvn clean install -DskipTests -f $WORKSPACE/rts/lra-examples/coordinator-quarkus
fi

export PORT=8787
export JDWP=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=

cd "$( dirname "${BASH_SOURCE[0]}" )"

CURL_IP_OPTS=""
IP_OPTS="${IPV6_OPTS}" # use setup of IPv6 if it's defined, otherwise go with IPv4
if [ -z "$IP_OPTS" ]; then
    IP_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses"
    CURL_IP_OPTS="-4"
fi

java ${IP_OPTS} -Dquarkus.http.port=8080 $(getDebugArgs $PORT) -jar $WORKSPACE/rts/lra-examples/coordinator-quarkus/target/lra-coordinator-quarkus-runner.jar &
ID1=$!
((PORT++))
java ${IP_OPTS} -Dquarkus.http.port=8081 $(getDebugArgs $PORT) -jar $WORKSPACE/rts/lra-examples/coordinator-quarkus/target/lra-coordinator-quarkus-runner.jar &
ID2=$!
((PORT++))
java ${IP_OPTS} -Dquarkus.http.port=8082 -Dlra.http.port=8080 $(getDebugArgs $PORT) -jar hotel-service/target/quarkus-app/quarkus-run.jar &
ID3=$!
((PORT++))
java ${IP_OPTS} -Dquarkus.http.port=8083 -Dlra.http.port=8081 $(getDebugArgs $PORT) -jar flight-service/target/quarkus-app/quarkus-run.jar &
ID4=$!
((PORT++))
java ${IP_OPTS} -Dquarkus.http.port=8084 -Dlra.http.port=8080 $(getDebugArgs $PORT) -jar trip-controller/target/quarkus-app/quarkus-run.jar &
ID5=$!
((PORT++))

wait_for_all_quarkus_apps

MAVEN_OPTS=${IP_OPTS} mvn -f trip-client/pom.xml exec:java -Dexec.args="confirm"
MAVEN_OPTS=${IP_OPTS} mvn -f trip-client/pom.xml exec:java -Dexec.args="cancel"

echo -e "\n\n\n"
BOOKINGID=$(curl ${CURL_IP_OPTS} -X POST "http://localhost:8084/?hotelName=TheGrand&flightNumber1=BA123&flightNumber2=RH456" -sS | jq -r ".id")
echo "Booking ID was: $BOOKINGID"

###### START not working
#When a coordinator killed and then restarted everything should keep working as usual
#instead when restarting the coordinator the final status of the nested LRAs is not correct
kill -9 $ID1
java ${IP_OPTS} -Dquarkus.http.port=8080 $(getDebugArgs 8787) -jar $WORKSPACE/rts/lra-examples/coordinator-quarkus/target/lra-coordinator-quarkus-runner.jar &
ID1=$!
########## END not working

wait_for_all_quarkus_apps
wait_for_recovery
echo -e "\n\n\n"

set +x
echo "Cancelling with curl ${CURL_IP_OPTS} -X DELETE http://localhost:8084/`urlencode $BOOKINGID`"
BOOKINIDENCODED=`urlencode $BOOKINGID`
set -x

echo $BOOKINIDENCODED
RESPONSE=$(curl ${CURL_IP_OPTS} -X DELETE http://localhost:8084/$BOOKINIDENCODED -sS)
echo -e "\nresponse is: \n $RESPONSE\n"

STATUS=$(echo $RESPONSE | jq -r ".status")
if [ "$STATUS" != "CANCELLED" ]; then
    echo "The status is not 'CANCELLED': $STATUS"
    exit -1
fi

if [ "$DEBUG" ]; then
    echo "Processes are still running ($ID1 $ID2 $ID3 $ID4 $ID5) press any key to end them"
    read
fi