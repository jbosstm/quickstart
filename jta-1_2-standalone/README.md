Standalone JTA 1.2 example.
===

Author: Gytis Trikleris, Ondra Chaloupka<br/>
Level: Intermediate<br/>
Technologies: JTA, CDI<br/>

What is it?
---

This example demonstrates how to use JTA 1.2 annotations and CDI beans in standalone application.

JTA 1.2 introduces annotations for controlling the transactional behaviour of CDI beans.
This quickstart indicate how to take advantage of these annotations in a standalone application
(ie in environments where a Java EE container is either not available or is undesirable).


System requirements
---

All you need to build this project is Java 8 (Java SDK 1.8) or better and Maven 3.3.3 or better.


Build and Deploy the Quickstart
---

1. Open a command line and navigate to the root directory of this quickstart.
2. Execute quickstart:

```
mvn clean test
```
