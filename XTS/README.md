XTS Quickstarts
===============

Examples using WS-AtomicTransaction and WS-BusinessActivity.


Simple Quickstarts
==================

These quickstarts are a good place to start if you are new to XTS or our new APIs.


JTA over WS-AT
--------------
This example demonstrates a JTA client that invokes a remote EJB over Web services. The JTA transaction is distributed to the remote EJB using WS-AtomicTransaction.

WS-BA to JTA
------------
This example shows you how to use a distributed compensation based transaction to make updates to a database in a JTA transaction.

WS-BA Simple
------------
The example shows how transactional guarantees can be provided in situations where a rollback is not applicable. A Compensation-based transactions
is provided via WS-BusinessActivity.


Legacy API Quickstarts
======================

These quickstarts have not yet been updated to use the improved APIs. They will be migrated to the new APIs in the future.
We have created Jira issues for each demo; so if you want it updating sooner, please vote on the issue!

You may still find these examples of use if you are using an old version of Narayana (JBossTS) or if you need a feature
not yet covered by the new APIs.

Demo
----
A demo application that shows how many of the XTS features can be orchestrated into a distributed application.

Vote here to have it updated to the new APIs: https://issues.jboss.org/browse/JBTM-1441

WS-AT to JTA (Multi Hop)
------------------------
This example is similar to "JTA over WS-AT"; however the service also acts as a client to a second service. Again using WS-AT
to distribute the transaction over Web services.

Vote here to have it updated to the new APIs: https://issues.jboss.org/browse/JBTM-1473

WS-AT to JTA (Multi Service)
----------------------------
This example is similar to "JTA over WS-AT". However, the client invokes two services instead of one.

Vote here to have it updated to the new APIs: https://issues.jboss.org/browse/JBTM-1474

