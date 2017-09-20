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
   CYGWIN*) export NARAYANA_INSTALL_LOCATION=`cygpath -w $(pwd)/narayana-full-5.6.5.Final-SNAPSHOT/` ;;
   *)       export NARAYANA_INSTALL_LOCATION=$(pwd)/narayana-full-5.6.5.Final-SNAPSHOT/ ;;
esac

rm -rf $NARAYANA_INSTALL_LOCATION
unzip $WORKSPACE/narayana-full-5.6.5.Final-SNAPSHOT-bin.zip

java $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-swarm.jar -Dswarm.http.port=8080 -Dswarm.transactions.object-store-path=../txlogs &
ID1=$!
((PORT++))
java $(getDebugArgs $PORT) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-swarm.jar -Dswarm.http.port=8081 &
ID2=$!
((PORT++))
java $(getDebugArgs $PORT) -jar hotel-service/target/lra-test-swarm.jar -Dswarm.http.port=8082 &
ID3=$!
((PORT++))
java $(getDebugArgs $PORT) -jar flight-service/target/lra-test-swarm.jar -Dswarm.http.port=8083 -Dlra.http.port=8081 &
ID4=$!
((PORT++))
java $(getDebugArgs $PORT) -jar trip-controller/target/lra-test-swarm.jar -Dswarm.http.port=8084 -Dlra.http.port=8080 &
ID5=$!
((PORT++))

echo "Waiting for all the servers to start"
sleep 40

mvn -f trip-client/pom.xml exec:java -Dservice.http.host="localhost" -Dservice.http.port=8084 -Dexec.args=confirm
mvn -f trip-client/pom.xml exec:java -Dservice.http.host="localhost" -Dservice.http.port=8084 -Dexec.args=cancel

echo -e "\n\n\n"
BOOKINGID=`curl -X POST http://localhost:8084/?hotelName=TheGrand\&flightNumber=BA123\&flightNumber2=RH456 -sS | jq -r ".id"`
echo "The booking ID for http://localhost:8084/?hotelName=TheGrand\&flightNumber=BA123\&flightNumber2=RH456 was: $BOOKINGID"
kill -9 $ID1
java $(getDebugArgs 8787) -jar $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-swarm.jar -Dswarm.http.port=8080 -Dswarm.transactions.object-store-path=../txlogs &
ID1=$!
echo "Waiting for all the coordinator to recover"
sleep 20
echo -e "\n\n\n"
curl -X PUT http://localhost:8084/`urlencode $BOOKINGID`
echo ""

[ $DEBUG ] && echo "Processes are still running ($ID1 $ID2 $ID3 $ID4 $ID5) press any key to end them" && read
kill -9 $ID1 $ID2 $ID3 $ID4 $ID5

rm -rf $NARAYANA_INSTALL_LOCATION