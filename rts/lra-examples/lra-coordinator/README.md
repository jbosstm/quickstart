# Narayana implementation of a coordinator for LRAs

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification of an API that
enables loosely coupled services to coordinate long running activities in such a way as to
guarantee a globally consistent outcome without the need to take long duration locks on data.

This module contains thorntail and quarkus jars for running a standalone LRA coordinator (a Narayana
specific module for coordinating participants involved in an LRA.

To start a coordinator in a thorntail container listening for requests on port 8080 run the following command:

> java -Dthorntail.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar target/lra-coordinator-thorntail.jar &

The system property `thorntail.http.port` defines the port on which the coordinator listens for requests.
The coordinator relies on persistent storage for
creating logs in order to be able to recover from failures. Normally thorntail uses a temporary
directory for its logs which is not useful in recovery situations since the storage needs to
be persistent. Therefore you must to overide this default using the following system property:

> -Dthorntail.transactions.object-store-path=../txlogs

Note that this module implements the backend management of LRAs and is REST based.
It is possible to connect to this proprietary functionality as a client or participant
making it possible to include non Java based participants and clients. The coordinator
endpoints are documented using MicroProfile OpenAPI annotations.

Although in the example the coordinator runs in a thorntail container, it is possible to run it using a quarkus fat jar
listening for requests on port 8080 and using a transaction log store hard-coded to the directory `ObjectStore` in the
user directory (ie the value of the system property `user.dir`) run the following command:

> java -jar target/lra-coordinator-quarkus.jar &

But note that, due to lack of configurability, the quarkus version in this module is for
demonstration purposes only. To build a configurable coordinator using quarkus use
the `cdi-embedded` example as a guide. The config is located in the application resources
directory (`../cdi-embedded/src/main/resources`).
