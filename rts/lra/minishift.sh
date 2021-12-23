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

persistentVolumeClaimDef() {
cat << EOF
apiVersion: "v1"
kind: "PersistentVolumeClaim"
metadata:
  name: "${1}"
spec:
  accessModes:
    - "ReadWriteOnce"
  resources:
    requests:
      storage: "100Mi"
EOF
}

NARAYANA_LRA_COORDINATOR_LOCATION="$NARAYANA_INSTALL_LOCATION/rts/lra/lra-coordinator/target/lra-coordinator-runner.jar"
if [ ! -f "$NARAYANA_LRA_COORDINATOR_LOCATION" ]; then
  echo "You need to set NARAYANA_INSTALL_LOCATION correctly, file '$NARAYANA_LRA_COORDINATOR_LOCATION' not found"
  exit
fi

oc get projects | grep lrademo
if [ $? = ] ]; then
  echo "Waiting for lrademo to terminate"
  exit
fi

set -e

# Create a new project
oc new-project lrademo


# creating persistent volume claims used in apps
# manually can be created in the webconsole `minishift console` -> Storage
persistentVolumeClaimDef "lra-coordinator-logs" | oc create -f -
persistentVolumeClaimDef "flight-lra-coordinator-logs" | oc create -f -

# Deploy the LRA coordinator
cd lra-coordinator && rm -rf target/ && mkdir target
oc new-build --binary --name=lra-coordinator -l app=lra-coordinator
cp "$NARAYANA_LRA_COORDINATOR_LOCATION" target/
oc start-build lra-coordinator --from-dir=. --follow
oc new-app lra-coordinator -l app=lra-coordinator
oc volume dc/lra-coordinator --add -t persistentVolumeClaim --claim-name lra-coordinator-logs -m /data
oc expose service lra-coordinator
# Deploy the Flight LRA coordinator
oc new-build --binary --name=flight-lra-coordinator -l app=flight-lra-coordinator
cp "$NARAYANA_LRA_COORDINATOR_LOCATION" target/
oc start-build flight-lra-coordinator --from-dir=. --follow
oc new-app flight-lra-coordinator -l app=flight-lra-coordinator
oc volume dc/flight-lra-coordinator --add -t persistentVolumeClaim --claim-name flight-lra-coordinator-logs -m /data
oc expose service flight-lra-coordinator
cd ..

# Deploy the Flight Service
cd flight-service
mvn clean package
oc new-build --binary --name=flight -l app=flight
oc start-build flight --from-dir=. --follow
oc new-app flight -l app=flight
oc expose service flight
cd ..
# Deploy the Hotel Service
cd hotel-service
mvn clean package
oc new-build --binary --name=hotel -l app=hotel
oc start-build hotel --from-dir=. --follow
oc new-app hotel -l app=hotel
oc expose service hotel
cd ..
# Deploy the Trip Controller
cd trip-controller
mvn clean package
oc new-build --binary --name=trip -l app=trip
oc start-build trip --from-dir=. --follow
oc new-app trip -l app=trip
oc expose service trip
cd ..

# You can then run the client:
echo "Waiting for app to deploy"
sleep 60
cd trip-client
mvn clean package
mvn exec:java -Dservice.http.host="trip-lrademo.`minishift ip`.nip.io" -Dservice.http.port=80 -Dlra.coordinator.url="http://lra-coordinator-lrademo.`minishift ip`.nip.io:80/lra-coordinator" -Dexec.args=cancel
mvn exec:java -Dservice.http.host="trip-lrademo.`minishift ip`.nip.io" -Dservice.http.port=80 -Dlra.coordinator.url="http://lra-coordinator-lrademo.`minishift ip`.nip.io:80/lra-coordinator" -Dexec.args=confirm
cd -

echo -e "\n\n\n"
BOOKINGID=$(curl -X POST "http://trip-lrademo.`minishift ip`.nip.io/?hotelName=TheGrand&flightNumber1=BA123&flightNumber2=RH456" -sS | jq -r ".id")
echo "The booking ID for http://trip-lrademo.`minishift ip`.nip.io/?hotelName=TheGrand&flightNumber1=BA123&flightNumber2=RH456 was: $BOOKINGID"
cd lra-coordinator
oc start-build lra-coordinator --from-dir=. --follow
cd -
echo "Waiting for the coordinator to recover"
sleep 30
set +e
until curl -X GET http://lra-coordinator-lrademo.`minishift ip`.nip.io/lra-coordinator | jq
do
    echo "Waiting to try again"
    sleep 10
done
set -e
echo "LRA coordinator"
curl -X GET http://lra-coordinator-lrademo.`minishift ip`.nip.io/lra-coordinator -sS | jq
echo "Flight LRA coordinator"
curl -X GET http://flight-lra-coordinator-lrademo.`minishift ip`.nip.io/lra-coordinator -sS | jq
echo "HOTEL"
curl -X GET http://hotel-lrademo.`minishift ip`.nip.io -sS | jq
echo "FLIGHT"
curl -X GET http://flight-lrademo.`minishift ip`.nip.io -sS | jq
echo "TRIP"
curl -X GET http://trip-lrademo.`minishift ip`.nip.io -sS | jq
echo -e "\n\n\n"


echo -e "\n\n\n"
echo "Confirming with curl -X PUT http://trip-lrademo.`minishift ip`.nip.io/`urlencode $BOOKINGID`"
curl -X PUT http://trip-lrademo.`minishift ip`.nip.io/`urlencode $BOOKINGID`
echo ""
