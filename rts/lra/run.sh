set -e

cd "$( dirname "${BASH_SOURCE[0]}" )"

export NARAYANA_INSTALL_LOCATION=$(pwd)/narayana-full-5.6.5.Final-SNAPSHOT/
rm -rf $NARAYANA_INSTALL_LOCATION
unzip $WORKSPACE/narayana-full-5.6.5.Final-SNAPSHOT-bin.zip

cd $NARAYANA_INSTALL_LOCATION
java -jar rts/lra/lra-coordinator-swarm.jar -Dswarm.http.port=8080 &
ID1=$!
java -jar rts/lra/lra-coordinator-swarm.jar -Dswarm.http.port=8081 &
ID2=$!
cd -
java -jar hotel-service/target/lra-test-swarm.jar -Dswarm.http.port=8082 &
ID3=$!
java -jar flight-service/target/lra-test-swarm.jar -Dswarm.http.port=8083 -Dlra.http.port=8081 &
ID4=$!
java -jar trip-controller/target/lra-test-swarm.jar -Dswarm.http.port=8084 -Dlra.http.port=8080 &
ID5=$!

echo "Waiting for all the servers to start"
sleep 60

mvn -f trip-client/pom.xml exec:java -Dservice.http.host="localhost" -Dservice.http.port=8084 -Dexec.args=confirm
mvn -f trip-client/pom.xml exec:java -Dservice.http.host="localhost" -Dservice.http.port=8084 -Dexec.args=cancel

kill -9 $ID1 $ID2 $ID3 $ID4 $ID5

rm -rf $NARAYANA_INSTALL_LOCATION