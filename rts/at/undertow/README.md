OVERVIEW
--------
This example shows how you can make your web services transactional using undertow and resteasy.

Running the demo wil start 3 Undertow servers:
- one for the RESTAT coordinator
- two servers for running participants

You may drive the demo using any HTTP client. For linux users the quickstart includes a bash script which uses curl
as the client.

Alternatively, you may run the demo using programmed example (simply press enter after starting the quickstart)/


USAGE
-----

    mvn clean compile exec:java

EXPECTED OUTPUT
---------------

1. Three messages showing 
starting undertow (coordinator)
starting undertow (service 1)
starting undertow (service 2)

2. The quickstart now waits for the user to press enter

3. The quickstart begins a transaction by POSTing a request to the coordinator running in the first container.
   It then makes a transactional service request to the two services passing the coordinator enlistment
   url asking it to create a document with content "value".
   Each service enlists itself with the transaction manager
   The quickstart ends the transaction by PUTting to the coordinator.
   The coordinator PUTs to each participant to tell it to prepare and commit:

Service ep http://localhost:8094/eg/service/2/terminator: PUT request to terminate url: wId=2, status:=txstatus=TransactionPrepared
Service ep http://localhost:8092/eg/service/1/terminator: PUT request to terminate url: wId=1, status:=txstatus=TransactionPrepared
Service ep http://localhost:8092/eg/service/1/terminator: PUT request to terminate url: wId=1, status:=txstatus=TransactionCommitted
Service ep http://localhost:8094/eg/service/2/terminator: PUT request to terminate url: wId=2, status:=txstatus=TransactionCommitted

4. The client checks that the services got the commit requests by asking them for the contents of the document:

Service 1 value:value
Service 2 value:value

INTERACTIVE USAGE
-----------------

After starting the quickstart the example pauses waiting for the user to press enter. This is so that the user
can start and stop transactions and make transactional service requests manually from the command line.

For linux users we provide a shell script to facilitate this manual interaction with the quickstart. Make sure it is
executable using the chmod command:

    chmod 755 txctl.sh

For help type:

    txctl.sh

For example to start a transaction, perform a transaction service request to each participant, end the transaction and finally query each service for the contents of the document that was updated:

txctl.sh -s 1000000 # start transaction
txctl.sh -w http://localhost:8092/eg/service newVal <enlist url> # enlist service 1
txctl.sh -w http://localhost:8094/eg/service newVal <enlist url> # enlist service 2
txctl.sh -c <txn terminator url> # end the transaction

txctl.sh -q http://localhost:8092/eg/service # query the value to see if the commit updated it
txctl.sh -q http://localhost:8094/eg/service # query the value to see if the commit updated it

The following is an example of the expected output:

```
[mmusgrov@localhost meetings](master)$ txctl.sh
syntax:
 /home/mmusgrov/bin/txctl.sh -s [timeout]            - start a transaction (with an optional timeout)
 /home/mmusgrov/bin/txctl.sh -l                      - list active transactions 
 /home/mmusgrov/bin/txctl.sh -i <txn URL>            - show enlistment and terminator urls for a give txn url
 /home/mmusgrov/bin/txctl.sh -c <term URL>           - commit a transaction
 /home/mmusgrov/bin/txctl.sh -a <term URL>           - abort a transaction
 /home/mmusgrov/bin/txctl.sh -w <service URL> <value> <enlist URL> - ask a web service to transactionally update a value
 /home/mmusgrov/bin/txctl.sh -q <service URL>        - ask a web service for its current value
[mmusgrov@localhost meetings](master)$ txctl.sh -s 1000000
HTTP/1.1 201 Created
Connection: keep-alive
Location: http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2
Content-Length: 0
Link: <http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2/terminator>; rel="terminator"; title="terminator"
Link: <http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2>; rel="durable-participant"; title="durable-participant"
Link: <http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2/volatile-participant>; rel="volatile-participant"; title="volatile-participant"
Date: Tue, 12 May 2015 09:59:43 GMT

[mmusgrov@localhost meetings](master)$ txctl.sh -w http://localhost:8092/eg/service newVal http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: text/plain
Content-Length: 1
Date: Tue, 12 May 2015 09:59:57 GMT

1[mmusgrov@localhost meetings](master)$ txctl.sh -w http://localhost:8094/eg/service newVal http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_5551cf0b_2
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: text/plain
Content-Length: 1
Date: Tue, 12 May 2015 10:00:05 GMT

2[mmusgrov@localhost meetings](master)$ txctl.sh -c http://localhost:8090/tx/transaction-manager/0_ffff7f000001_d1da_551cf0b_2/terminator
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/octet-stream
Content-Length: 29
Date: Tue, 12 May 2015 10:00:16 GMT

txstatus=TransactionCommitted[mmusgrov@localhost meetings](master)$ 
[mmusgrov@localhost meetings](master)$ txctl.sh -q http://localhost:8092/eg/service
newVal[mmusgrov@localhost meetings](master)$ txctl.sh -q http://localhost:8094/eg/service
newVal[mmusgrov@localhost meetings](master)$ 
```

