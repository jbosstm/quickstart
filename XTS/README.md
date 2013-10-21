XTS Quickstarts
===============

These quickstarts demonstrate the various ways of distributing a transaction over Web Services. If you need to distribute a JTA
transaction over Web Services, you are most likely to be interested in looking at the "WS-AT to JTA" examples. These examples
demonstrate just how simple it is to achieve this functionality.

If you are interested in using WS-Business Activity for distributing a compensating transaction, you should look at our examples in
the "compensating-transactions" directory in the root of the Narayana quickstarts. Here we provide a user-friendly annotations-based
API for using compensating transactions.

Finally, if you are interested in a lower-level API for dealing with all aspects of the WS-AT and WS-BA protocol, you may find our
"Raw XTS API demo" to be useful.


WS-AT to JTA (Multi Hop)
------------------------
This example demonstrates a JTA client that invokes a remote EJB over Web services. The JTA transaction is distributed to the remote EJB using WS-AtomicTransaction.
The service also acts as a client to a second service. Again using WS-AT to distribute the transaction over Web services.


WS-AT to JTA (Multi Service)
----------------------------
This example is similar to "WS-AT to JTA (Multi Hop)". However, the client invokes two services instead of one.


Raw XTS API Demo
----------------
An example that demonstrates the usage of the low-level raw API. This example is good if you need to develop your own WS-AT
or WS-BA participants or if you need to use a particular feature not yet available in the higher-level APIs.