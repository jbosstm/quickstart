OVERVIEW
--------
A example that demonstrates how to use [REST-AT annotations](https://github.com/jbosstm/narayana/tree/main/rts/at/tx/src/main/java/org/jboss/jbossts/star/annotation) to define transactional microservices.

USAGE
--------------------

Prior to running the example make sure that the [REST-AT coordinator is deployed](../README.md#usage).

Once REST-AT coordinator is deployed build and run the annotation example application to run on port 8082, 8083 and 8084 as it in configured in application.properties
    
    cd <narayana-repo>/rts/at/annotation/
    mvn clean package -DskipTests
    java -jar flight-service/target/quarkus-app/quarkus-run.jar &
    java -jar hotel-service/target/quarkus-app/quarkus-run.jar &
    java -jar trip-service/target/quarkus-app/quarkus-run.jar &

Book a trip within a transaction, refer to the [`bookTrip` method in the trip service](./trip-service/src/main/java/io/narayana/sra/demo/api/TripController.java) to see how it is annotated. The following curl command will invoke the resource.
    
    curl -XPOST http://localhost:8082/trip/book?hotelName=Rex&flightNumber=123
    curl http://localhost:8082/trip/status?sraId={paste sraId here}



Since the `bookTrip` resource is annotated with `@SRA(value = SRA.Type.REQUIRED)`, the curl request will start a new REST-AT transaction and you can verify that it is running by querying the coordinator:  `curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager`.

EXPECTED OUTPUT
---------------
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating flight participant state to: TransactionPrepared
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating hotel participant state to: TransactionPrepared
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating trip participant state to: TransactionPrepared
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating trip participant state to: TransactionCommitted
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating hotel participant state to: TransactionCommitted
    SRA: 0_ffff0ad775f3_-2e0999fe_64d0a461_3f8: Updating flight participant state to: TransactionCommitted
    {"cancelPending":false,"details":[{"cancelPending":false,"details":[],"encodedId":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","id":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","name":"Rex","quantity":1,"sraId":"http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","status":"PROVISIONAL","type":"Hotel"},{"cancelPending":false,"details":[],"encodedId":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","id":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","name":"456","quantity":1,"sraId":"http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","status":"PROVISIONAL","type":"Flight"}],"encodedId":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","id":"0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","name":"Aggregate Booking","quantity":1,"sraId":"http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffff0ad775f3_-2e0999fe_64d0a461_3f8","status":"CONFIRMED","type":"Trip"}
    Confirming tripBooking id 0_ffff0ad775f3_-2e0999fe_64d0a461_4a4 (Aggregate Booking) status: CONFIRMED

WHAT JUST HAPPENED?
-------------------

1. Build and run the REST-AT annotation application, it starts all three microservice on different port i.e 8082, 8083 and 8084 which is configured in application.properties such as in [flight-service](flight-service/src/main/resources/application.properties), [hotel-service](hotel-service/src/main/resources/application.properties) and [trip-service](flight-service/src/main/resources/application.properties).

2. Invoke book trip which start REST-AT transaction and calls individual hotel and flight service issuing the POST request to the trip service.

> **_NOTE:_** The quickstart marks the services as REST-AT aware participants because they are extending a [helper](https://github.com/jbosstm/narayana/blob/main/rts/at/tx/src/main/java/org/jboss/jbossts/star/client/SRAParticipant.java) class that provides the relevant REST-AT support.