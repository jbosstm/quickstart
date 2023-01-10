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

_NOTE:_ Docker is used to instantiate a MongoDB container (the test will automatically carry out all steps needed to create the container).
Please, make sure that Docker is installed (and configured) in your environment.

_NOTE:_ The quickstart is tested to work with WildFly 28.0.0 and Narayana 6.0. It should work with newer versions, but they are not currently tested.

To run the quickstart: `mvn clean test -Parq`

Understanding the Code
----------------------

To understand the code and the Compensating Transactions API, it is recommended that you:

1. Browse the code reading the comments
2. Look at the other compensating transactions quickstarts
3. Read the following blog series: http://jbossts.blogspot.co.uk/2013/05/compensating-transactions-when-acid-is.html
