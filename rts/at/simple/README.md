OVERVIEW
--------
An example of how to start and end a transaction using REST style semantics.


USAGE
-----
Prior to running the example make sure that the [RESTAT coordinator is deployed](../README.md#usage).

    mvn clean compile exec:exec

or use the run script


EXPECTED OUTPUT
---------------
[The examples run under the control of maven so you will need to filter maven output from example output.]

    transaction running: txStatus=TransactionActive
    Success


WHAT JUST HAPPENED?
-------------------

1. We deployed a JAX-RS servlet that implements a RESTful interface to the Narayana transaction manager (TM).

2. The example started a transaction by POSTing to a well known url that the JAX-RS servlet was listening on.

3. The servlet looked up the TM running in the container it was deployed to and started a transaction.

4. The servlet returned a URL (the transaction URL) that the example can use to control the termination of the active transaction.

5. The example performed an HTTP GET request on the transaction URL to check that the transaction status was active.

6. The example counted the number of running transactions (in order to check that at least one was active).

7. The example performed an HTTP PUT request to the transaction URL thus terminating the transaction.

8. The example counted the number of running transactions in order to verify that there was one less transaction.

