# Narayana implementation of a coordinator for LRAs

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification of an API that
enables loosely coupled services to coordinate long running activities in such a way as to
guarantee a globally consistent outcome without the need to take long duration locks on data.

This module contains a runnable quarkus jar for running a standalone LRA coordinator (a Narayana
specific module for coordinating participants involved in an LRA).

To start a coordinator in a quarkus application, listening for requests on port 8080 run the following command:

> java -jar target/lra-coordinator-quarkus.jar &

The coordinator will then listen for requests on the default port of 8080.
The coordinator relies on persistent storage for creating logs in order
to be able to recover from failures. Unless otherwise specified these will
be created under a directory called `ObjectStore` in the the user directory
(ie the value of the system property `user.dir`).

Note that this module implements the backend management of LRAs and is REST based.
It is possible to connect to this proprietary functionality as a client or participant
making it possible to include non Java based participants and clients. The coordinator
endpoints are documented using MicroProfile OpenAPI annotations.

Note that, due to lack of configurability, the quarkus version in this module is for
demonstration purposes only. To build a configurable coordinator using quarkus use
the `cdi-embedded` example as a guide. The config is located in the application resources
directory (`../cdi-embedded/src/main/resources`).
