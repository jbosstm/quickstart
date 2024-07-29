JTA 1.2 in Wildfly example.
===

Author: Gytis Trikleris<br/>
Level: Intermediate<br/>
Technologies: JTA, JPA, JMS

What is it?
---

This example demonstrates how to use new JTA 1.2 features inside WildFly application server.


`QuickstartEntityRepository` and `QuickstartQueue` classes demonstrace usage of `@Transactional` annotation.
`TransactionScopedPojo` class demonstrates `@TransactionScoped` annotation. `TestCase` class is used to drive the example.


Build and Deploy the Quickstart
---

Please follow the information [here](../../README.md#obtain-wildfly) to obtain WildFly.

And then (the JBOSS_HOME being either the built version above, or from a downloaded version if it has the expected version of Narayana in it):
```
export JBOSS_HOME=<PATH_TO_JBOSS_HOME>
```

In order to run quickstart in the managed Wildfly application server, run the following command:

```
mvn clean install
```

In order to run quickstart in the remote Wildfly application server, run the following command:

```
mvn clean install -Parq
```
