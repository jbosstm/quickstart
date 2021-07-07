# MicroProfile LRA Participant Example with Embedded Coordinator

This example is similar to the [`cdi-participant`](#../cdi-participant/README.md) 
example. The difference is that this quickstart embeds an LRA coordinator
with the application.

## Start a service that takes part in LRAs

<pre>
java -jar target/quarkus-app/quarkus-run.jar &
</pre>

Then follow the instructions in the [`cdi-participant README.md file`](#../cdi-participant/README.md). For example,

<pre
curl -X PUT -I http://localhost:8080/cdi
</pre>

will send a JAX-RS request to an application endpoint annotated with `@LRA(LRA.Type.REQUIRED`.
The embedded coordinator will start an LRA just before the JAX-RS method is entered
and will end it just after the method finishes.
