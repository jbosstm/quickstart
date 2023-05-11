OVERVIEW
--------
An example of how to use REST-AT as individual micro services.

USAGE
--------------------
TEST SRA PARTICIPANTS

Start a REST-AT coordinator on port 8080

    cd <narayana-repo>/jboss-as/build/target/wildfly-28.0.0.Final
    cp docs/examples/configs/standalone-rts.xml standalone/configuration
    ./bin/standalone.sh -c standalone-rts.xml

VERIFY THAT IT IS RUNNING, eg:

    curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

BUILD AND RUN THE SRA EXAMPLE APPLICATION on port 8082,8083,8084 as it in configured in application.properties 

    cd <narayana-repo>/rts/at/sra-examples/flight-service
    mvn clean package -Dquarkus.package.type=uber-jar
    mvn quarkus:dev

    repeat the same step for hotel-service and trip-service
     
    This will start three micro service on three diffrent ports.

Book a trip within a transaction (see <narayana-repo>/rts/at/sra-examples/trip-service/sra/tripservice/api/TripController.java method bookTrip)
this starts a transaction and you can verify it is running using
curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

    curl -XPOST http://localhost:8082/trip/book?hotelName=Rex

EXPECTED OUTPUT
---------------
    SRA: 0_ffff0a4cf0b6_-568c278f_6458bef8_7fb: Updating hotel participant state to: TransactionPrepared

    SRA: 0_ffff0a4cf0b6_-568c278f_6458bef8_7fb: Updating trip participant state to: TransactionPrepared

    SRA: 0_ffff0a4cf0b6_-568c278f_6458bef8_7fb: Updating trip participant state to: TransactionCommitted

    SRA: 0_ffff0a4cf0b6_-568c278f_6458bef8_7fb: Updating hotel participant state to: TransactionCommitted


EXAMPLE OF HOW TO MANUALLY TEST THAT THE COORDINATOR IS WORKING USING curl


    curl -H "Content-Type: application/x-www-form-urlencoded" -X POST http://localhost:8080/rest-at-coordinator/tx/transaction-manager
    curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager
    curl -X PUT --data txstatus=TransactionCommitted http://localhost:8080/rest-at-coordinator/tx/transaction-manager/0_ffff0a4cf0b6_-568c278f_6458bef8_7fb/terminator
    curl http://localhost:8080/rest-at-coordinator/tx/transaction-manager

WHAT JUST HAPPENED?
-------------------

1. We started REST-AT coordinator which is available to use in wildfly.

2. We verified REST-AT coordinator by invoking the transaction manager url.

3. Build and run the sar-example application individually as shown, it starts all three microservice on different port i.e 8082,8083 and 8084.

4. Invoke book trip which start SRA and calls individual hotel and flight service.

5. The example performed an HTTP POST request and check that the transaction status code is 200.

6. The example performs commit operation if the status code is 200 in all the service.

7. The example performed rollback operation if the status code is not 200 in any of the service.

