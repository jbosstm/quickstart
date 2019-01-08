# MicroProfile LRA API Participant Example

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification of an API that
enables loosely coupled services to coordinate long running activities in such a way as to
guarantee a globally consistent outcome without the need to take long duration locks on data.

This module contains a swarm jar for running a standalone LRA coordinator.

For instructions for how to run it please refer to
[the description in the lra-examples README](../README.md#running-an-external-lra-coordinator)

Note that this module implements the backend management of LRAs and is REST based.
It is possible to connect to this proprietary functionality as a client or participant
making it possible to include non Java based participants and clients. The REST API
can be generated from swagger annotations by including the following dependency in the pom

```
        <dependency>
            <groupId>org.wildfly.swarm</groupId>
            <artifactId>swagger</artifactId>
            <version>2018.5.0</version>
        </dependency>

```

Now start a coordinator:

```
        java -Dswarm.http.port=8080 -Dswarm.transactions.object-store-path=../txlogs -jar target/lra-coordinator-swarm.jar                                                                                   
```

and proceed to http://localhost:8080/swagger.json where you will see the swagger definition of the API.
