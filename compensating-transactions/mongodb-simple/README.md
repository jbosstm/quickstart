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

Get a WildFly that has the expected version of Narayana in it. If you need to obtain it:
```
WORKSPACE=<QUICKSTARTS_REPO_ROOT> <QUICKSTARTS_REPO_ROOT>/scripts/hudson/quickstart.sh download_and_update_as
#You should be able to find a WildFly to use for JBOSS_HOME under <QUICKSTARTS_REPO_ROOT>/wildfly-<WILDFLY_VERSION>
```

And then (the JBOSS_HOME being either the built version above, or from a downloaded version if it has the expected version of Narayana in it):
```
export JBOSS_HOME=<PATH_TO_JBOSS_HOME>
mvn clean test -Parq
```

Understanding the Code
----------------------

To understand the code and the Compensating Transactions API, it is recommended that you:

1. Browse the code reading the comments
2. Look at the other compensating transactions quickstarts
3. Read the following blog series: http://jbossts.blogspot.co.uk/2013/05/compensating-transactions-when-acid-is.html
