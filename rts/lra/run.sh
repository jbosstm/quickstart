#!/usr/bin/env bash
function finish {
    if ps -p $ID1 > /dev/null
    then
       kill -9 $ID1
    fi

    if ps -p $ID2 > /dev/null
    then
       kill -9 $ID2
    fi

    if ps -p $ID3 > /dev/null
    then
       kill -9 $ID3
    fi

    if ps -p $ID4 > /dev/null
    then
       kill -9 $ID4
    fi

    if ps -p $ID5 > /dev/null
    then
       kill -9 $ID5
    fi

    if [ -d "$NARAYANA_INSTALL_LOCATION" ]; then
      rm -rf $NARAYANA_INSTALL_LOCATION
    fi
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
set -x

export PORT=8787
export JDWP=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=
function getDebugArgs {
  [ $DEBUG ] && echo "$JDWP"$1 || echo ""
}

cd "$( dirname "${BASH_SOURCE[0]}" )"

case "$(uname)" in
   CYGWIN*) export NARAYANA_INSTALL_LOCATION=`cygpath -w $(pwd)/narayana-full-6.0.2.Final-SNAPSHOT` ;;
   *)       export NARAYANA_INSTALL_LOCATION=$(pwd)/narayana-full-6.0.2.Final-SNAPSHOT ;;
esac

rm -rf $NARAYANA_INSTALL_LOCATION
NARAYANA_ZIP="narayana-full-6.0.2.Final-SNAPSHOT-bin.zip"
[ ! -f "$WORKSPACE/$NARAYANA_ZIP" ] &&\
   echo "There is no Narayana zip at \$WORKSPACE directory at '$WORKSPACE/$NARAYANA_ZIP" && exit 1
unzip "$WORKSPACE/$NARAYANA_ZIP"

CURL_IP_OPTS=""
IP_OPTS="${IPV6_OPTS}" # use setup of IPv6 if it's defined, otherwise go with IPv4
if [ -z "$IP_OPTS" ]; then
  IP_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses"
  CURL_IP_OPTS="-4"
fi

echo "Narayana installed location = $NARAYANA_INSTALL_LOCATION"
  java ${IP_OPTS} -Dquarkus.http.port=8080 $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar &
  ID1=$!
((PORT++))
  java ${IP_OPTS} -Dquarkus.http.port=8081 $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar &
  ID2=$!
((PORT++))
  java ${IP_OPTS} -Dquarkus.http.port=8082 $(getDebugArgs $PORT) -jar hotel-service/target/quarkus-app/quarkus-run.jar &
  ID3=$!
((PORT++))
  java ${IP_OPTS} -Dquarkus.http.port=8083 -Dlra.http.port=8081 $(getDebugArgs $PORT) -jar flight-service/target/quarkus-app/quarkus-run.jar &
  ID4=$!
((PORT++))
  java ${IP_OPTS} -Dquarkus.http.port=8084 -Dlra.http.port=8080 $(getDebugArgs $PORT) -jar trip-controller/target/quarkus-app/quarkus-run.jar &
  ID5=$!
((PORT++))

echo "Waiting for all the servers to start"
sleep `timeout_adjust 30 2>/dev/null || echo 30`

mvn -f trip-client/pom.xml exec:java -Dexec.args="${IP_OPTS} confirm"
mvn -f trip-client/pom.xml exec:java -Dexec.args="${IP_OPTS} cancel"

echo -e "\n\n\n"
BOOKINGID=$(curl ${CURL_IP_OPTS} -X POST "http://localhost:8084/?hotelName=TheGrand&flightNumber1=BA123&flightNumber2=RH456" -sS | jq -r ".id")
echo "Booking ID was: $BOOKINGID"

kill -9 $ID1
java ${IP_OPTS} -Dquarkus.http.port=8080 $(getDebugArgs 8787) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-runner.jar &
ID1=$!
echo "Waiting for all the coordinator to recover"
sleep `timeout_adjust 40 2>/dev/null || echo 40`
echo -e "\n\n\n"

set +x
echo "Confirming with curl ${CURL_IP_OPTS} -X PUT http://localhost:8084/`urlencode $BOOKINGID`"
curl ${CURL_IP_OPTS} -X PUT http://localhost:8084/`urlencode $BOOKINGID`
echo ""
set -x

[ $DEBUG ] && echo "Processes are still running ($ID1 $ID2 $ID3 $ID4 $ID5) press any key to end them" && read
