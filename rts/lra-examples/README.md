# MicroProfile LRA Examples

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification of an annotation based API that
enables loosely coupled services to coordinate long running activities in such a way as to
guarantee a globally consistent outcome without the need to take long duration locks on data.

First build the examples:

> mvn clean package

One quickstart is included:

## cdi-participant

[Shows a service which registers with an external LRA coordinator using CDI annotations](#cdi-participant/README.md)
