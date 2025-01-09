
# start the coordinator
java -jar narayana-lra-coordinator/target/lra-coordinator-runner.jar &

# start the service
java -Dquarkus.http.port=8081 -jar participant/target/quarkus-app/quarkus-run.jar &
# start the hot standby
java -Dquarkus.http.port=8082 -jar participant/target/quarkus-app/quarkus-run.jar &

sleep 1 # wait for the services to start

# invoke the service endpoint - when the completion endpoint is called by the coordinator
# the completion logic sends a request to the recovery coordinator to update the completion endpoint
curl -X PUT -I http://localhost:8081/migrate

# when recovery next runs the new completion endpoint will be invoked and hot standby service's completion
# endpoint will be called and will print the string "completed".

# sleep 2 TODO trigger a recovery pass instead of waiting

# ask the hot standby if completed
res=$(curl --write-out -I http://localhost:8082/migrate/completed)

# verify that res is true
