
# run 10 instances of a theatre verticle (each sharing a volatile STM object), all listening for
# requests/messages on HTTP port 8080.
function demo1 {
  # start the theatre verticle
  mvn exec:java -Pdemo1

  # make two bookings
  curl -X POST http://localhost:8080/api/theatre/Apollo
  curl -X POST http://localhost:8080/api/theatre/Savoy

  # check that booking counts are the same regardless of which verticle instance services it
  curl -X GET http://localhost:8080/api/theatre 
  curl -X GET http://localhost:8080/api/theatre
}

# same as demo1 but with persistent STM objects shared across JVMs
function demo2 {
  # start the theatre verticle
  mvn exec:java -Pdemo2

  # start a second theatre verticle and tell it clone the STM object
  mvn exec:java -Pdemo2 -Duid=0:ffffac1182c6:9197:5912d4ff:1

  # Create two bookings using services running in the two different JVMs 
  curl -X POST http://localhost:8080/api/theatre/Apollo
  curl -X POST http://localhost:8082/api/theatre/Savoy

  # Check that each JVM reports the correct number of bookings (namely 2)
  curl -X GET http://localhost:8080/api/theatre
  curl -X GET http://localhost:8082/api/theatre 
}

# Managing shared state across different STM objects
function demo3 {
  # start the trip service (which uses STM objects for making theatre and taxi bookings)
  mvn exec:java -Pdemo3

  # Make two trip bookings:

  curl -X POST http://localhost:8080/api/trip/Savoy/ABC
  curl -X POST http://localhost:8080/api/trip/Apollo/XYZ

  # and a single theatre booking:

  curl -X POST http://localhost:8080/api/theatre/Savoy

  # observe that each booking is serviced by a different verticle and that the theatre and taxi
  # bookng counts are correct (3 and 2)

  curl -X GET http://localhost:8080/api/theatre
  curl -X GET http://localhost:8080/api/taxi
}

# stress test
function demo4 {
  # Start the theatre service in one window or in the background:
  mvn exec:java -Pdemo1 &

  # and now make lots of concurrent bookings:
  mvn exec:java -Pstress
}
