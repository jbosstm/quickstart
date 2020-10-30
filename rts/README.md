
OVERVIEW
--------

Examples showing how to use transactions in a REST based design

at (REST Atomic Transactions)
--

Quickstarts that show the atomic transactions (as opposed to other models such as compensation based).

lra (Long running actions)
--
The LRA proposal introduces annotations and APIs for services to coordinate long running activities whilst still maintaining loose coupling and doing so in such a way as to guarantee a globally consistent outcome without the need to take locks on data.

Quickstart shows an flight booking application example using long running actions.

lra-example 
--
Here are few Quickstarts.
1. **cdi-participant** : The cdi quickstart takes a normal WAR and wraps it into a -thorntail runnable jar using the thorntail-maven-plugin
2. **lra-coordinator** : This quickstart contains a thorntail jar for running a standalone LRA coordinator (a Narayana
                         specific module for coordinating participants involved in an LRA.
3. **lra-jwt** : Quickstart for securing LRA coordinator's endpoints with JWT (Json web token) mechanism on WildFly application server