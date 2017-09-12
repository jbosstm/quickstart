set -e

cd ../../
export NARAYANA_INSTALL_LOCATION=$(pwd)
cd -
echo $NARAYANA_INSTALL_LOCATION

minishift delete
minishift start --vm-driver virtualbox

# Create a new project
oc new-project lra-minishift

# Deploy the LRA coordinator
cd lra-coordinator && rm -rf target/ && mkdir target
oc new-build --binary --name=lra-coordinator -l app=lra-coordinator
cp $NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator-swarm.jar target/
sleep 10
oc start-build lra-coordinator --from-dir=. --follow
oc new-app lra-coordinator -l app=lra-coordinator
oc expose service lra-coordinator
# Deploy the Flight LRA coordinator
oc new-build --binary --name=flight-lra-coordinator -l app=flight-lra-coordinator
sleep 10
oc start-build flight-lra-coordinator --from-dir=. --follow
oc new-app flight-lra-coordinator -l app=flight-lra-coordinator
oc expose service flight-lra-coordinator
cd ..

# Deploy the Flight Service
cd flight-service
oc new-build --binary --name=flight-service -l app=flight-service
mvn clean package
oc start-build flight-service --from-dir=. --follow
oc new-app flight-service -l app=flight-service
oc expose service flight-service
cd ..
# Deploy the Hotel Service
cd hotel-service
oc new-build --binary --name=hotel-service -l app=hotel-service
mvn clean package
oc start-build hotel-service --from-dir=. --follow
oc new-app hotel-service -l app=hotel-service
oc expose service hotel-service
cd ..
# Deploy the Trip Controller
cd trip-controller
oc new-build --binary --name=trip-controller -l app=trip-controller
mvn clean package
oc start-build trip-controller --from-dir=. --follow
oc new-app trip-controller -l app=trip-controller
oc expose service trip-controller
cd ..

# You can then run the client:
cd trip-client
echo "Waiting 10 seconds for app to deploy"
sleep 10
mvn exec:java -Dservice.http.host="trip-controller-lra-minishift.`minishift ip`.nip.io" -Dservice.http.port=80 -Dexec.args=cancel
mvn exec:java -Dservice.http.host="trip-controller-lra-minishift.`minishift ip`.nip.io" -Dservice.http.port=80 -Dexec.args=confirm
# This is interactive and allows you to confirm or cancel