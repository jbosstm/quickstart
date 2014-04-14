mongodb-simple: Use a Compensating Transaction to update two documents atomically
=======================================================
Author: Paul Robinson

Introduction
------------

This quickstart uses a compensating transaction to update two documents atomically. The example covers a banking use-case
in which each users' account details and balance are confined to their own documents. A money transfer operation comprises
of two document updates; a decrement of one user's balance and an increment of the other's. It is important that a partial
update does not occur, therefore it is essential that either both documents are update or neither are.

MongoDB supports atomic updates to single documents, but not to multiple documents. Therefore, it is not possible to fulfill
this use-case with MongoDB alone.

This quickstart shows how the Narayana Compensating Transactions API can be used to fulfill this use-case. Furthermore
it does so in a scalable manor that is compatible with a sharded environment.


Running the Quickstart
----------------------

_NOTE:_ You must have MongoDB installed, prior to running this quickstart. Also, it is assumed your MongoDB instance
will be running on localhost:27017. If this is not the case, you should update the
`MongoClient mongo = new MongoClient("localhost", 27017);` line in the AccountManager class, to reflect the actual
location of your server

_NOTE:_ The quickstart is tested to work with WildFly 8.0.0.Final and Narayana 5.0.0.Final. It should work with newer versions,
but they are not currently tested.

Console 1

    mongod

Console 2

    mvn clean test


Understanding the Code
----------------------

To understand the code and the Compensating Transactions API, it is recommended that you:

1. Browse the code reading the comments
2. Look at the other compensating transactions quickstarts
3. Read the following blog series: http://jbossts.blogspot.co.uk/2013/05/compensating-transactions-when-acid-is.html