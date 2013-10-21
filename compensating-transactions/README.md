Compensating Transactions Quickstarts
=====================================

These examples demonstrate the use of our API for developing applications that use Compensating Transactions. This API
provides an alternative to ACID transactions which is often a good alternative in situations where ACID is not apropriate,
yet some of the transactional guarantees are still required.

See the following series of articles for a fuller description of when to use compensating transactions: http://jbossts.blogspot.co.uk/2013/05/compensating-transactions-when-acid-is.html


Non-transactional Resource
--------------
This simple example shows how a non-transactional activity (such as sending an email, or printing a document) can be coordinated
in a compensating transaction.


Travel Agent
------------
This more complex example shows how a long running compensating transaction can be composed of a series of short-running
ACID transactions. The example also involves multiple organisations and forms a distributed transaction over Web Services.

