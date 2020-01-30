#!/usr/bin/env bash
function finish {
    kill -9 $ID1 $ID2 $ID3 $ID4 $ID5

    rm -rf $NARAYANA_INSTALL_LOCATION
}
trap finish EXIT

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

set -e

export PORT=8787
export JDWP=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=
function getDebugArgs {
  [ $DEBUG ] && echo "$JDWP"$1 || echo ""
}

cd "$( dirname "${BASH_SOURCE[0]}" )"

case "$(uname)" in
   CYGWIN*) export NARAYANA_INSTALL_LOCATION=`cygpath -w $(pwd)/narayana-full-5.10.5.Final-SNAPSHOT-SNAPSHOT/` ;;
   *)       export NARAYANA_INSTALL_LOCATION=$(pwd)/narayana-full-5.10.5.Final-SNAPSHOT-SNAPSHOT/ ;;
esac

rm -rf $NARAYANA_INSTALL_LOCATION
NARAYANA_ZIP="narayana-full-5.10.5.Final-SNAPSHOT-SNAPSHOT-bin.zip"
[ ! -f "$WORKSPACE/$NARAYANA_ZIP" ] &&\
   echo "There is no Narayana zip at \$WORKSPACE directory at '$WORKSPACE/$NARAYANA_ZIP" && exit 1
unzip "$WORKSPACE/$NARAYANA_ZIP"

java -Dquarkus.http.port=8080 $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar -Dthorntail.transactions.object-store-path=../lra-coordinator-logs &
ID1=$!
((PORT++))
java -Dquarkus.http.port=8081 $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar -Dthorntail.transactions.object-store-path=../flight-lra-coordinator-logs &
ID2=$!
((PORT++))
java -Dthorntail.http.port=8082 $(getDebugArgs $PORT) -jar hotel-service/target/lra-test-thorntail.jar &
ID3=$!
((PORT++))
java -Dthorntail.http.port=8083 -Dlra.http.port=8081 $(getDebugArgs $PORT) -jar flight-service/target/lra-test-thorntail.jar &
ID4=$!
((PORT++))
java -Dthorntail.http.port=8084 -Dlra.http.port=8080 $(getDebugArgs $PORT) -jar trip-controller/target/lra-test-thorntail.jar &
ID5=$!
((PORT++))

echo "Waiting for all the servers to start"
sleep 30

mvn -f trip-client/pom.xml exec:java -Dexec.args=confirm
mvn -f trip-client/pom.xml exec:java -Dexec.args=cancel

echo -e "\n\n\n"
BOOKINGID=$(curl -X POST "http://localhost:8084/?hotelName=TheGrand&flightNumber1=BA123&flightNumber2=RH456" -sS | jq -r ".id")
echo "Booking ID was: $BOOKINGID"
kill -9 $ID1
java -Dquarkus.http.port=8080 $(getDebugArgs 8787) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar -Dthorntail.transactions.object-store-path=../lra-coordinator-logs &
ID1=$!
echo "Waiting for all the coordinator to recover"
sleep 40
echo -e "\n\n\n"

echo "Confirming with curl -X PUT http://localhost:8084/`urlencode $BOOKINGID`"
curl -X PUT http://localhost:8084/`urlencode $BOOKINGID`
echo ""

[ $DEBUG ] && echo "Processes are still running ($ID1 $ID2 $ID3 $ID4 $ID5) press any key to end them" && read
finish
