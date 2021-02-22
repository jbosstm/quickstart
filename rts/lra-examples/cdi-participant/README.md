# MicroProfile LRA Participant Example

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification
of an API that enables loosely coupled services to coordinate long running
activities in such a way as to guarantee a globally consistent outcome without the
need to take long duration locks on data.

The cdi example takes a normal WAR and wraps it into a -thorntail runnable jar
using the `thorntail-maven-plugin`:

```
    <plugin>
      <groupId>io.thorntail</groupId>
      <artifactId>thorntail-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>package</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
```

The example assumes that an external LRA coordinator is running.

It is also possible to run the example as a quarkus application (see project 
[`cdi-embedded`](#../cdi-embedded/README.md) which runs
with a coordinator embedded with the application).

## Running an external LRA coordinator

This example requires an external LRA coordinator.
The [`lra-coordinator`](#../lra-coordinator/README.md)  maven module contains
a project for building a thorntail or quarkus jar that runs a standalone LRA coordinator.

So, assuming you are in the cdi example directory, to start a coordinator type:

<pre>
  java -Dthorntail.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar ../lra-coordinator/target/lra-coordinator-thorntail.jar
or
  java -jar ../lra-coordinator/target/lra-coordinator-quarkus.jar &
</pre>


## Start a service that takes part in LRAs

The example requires the Eclipse MicroProfile LRA annotations and the name of the
JAX-RS header that exposes the id of any LRA context during JAX-RS invocations:

```
        <dependency>
            <groupId>io.narayana.microprofile.lra</groupId>
            <artifactId>microprofile-lra-api</artifactId>
            <version>${version.microprofile.lra}</version>
        </dependency>
```

The spec implementation of the LRA annotations is provided by JAX-RS filters which
are activated by including the following dependency:

```
        <dependency>
            <groupId>org.jboss.narayana.rts</groupId>
            <artifactId>narayana-lra</artifactId>
            <version>${project.version}</version>
        </dependency>
```

Start the service on port 8081. Also set the system property, `thorntail.http.port`, 
which defines the port on which the coordinator is listening. 

<pre>
java -Dthorntail.http.port=8081 -Dlra.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar ./target/lra-participant-example-thorntail.jar
</pre>

## Invoke a service method that will run in the context of an LRA

In another terminal send a request to the service using curl:

<pre>
curl -X PUT -I http://localhost:8081/cdi
</pre>

<pre>
2019-10-07 16:39:55,474 INFO  [stdout] (default task-2) 1 completions
HTTP/1.1 204 No Content
Date: Sun, 14 Oct 2018 10:51:15 GMT
</pre>

This curl request has invoked a service method which is annotated with
`@LRA(LRA.Type.REQUIRED)`. This annotation tells the implementation to
start an LRA before just before the JAX-RS method is entered. Since the
service resource contains a method annotated with `@Compensate` the
resource will be also be enlisted in the new LRA before the method is invoked.

When the method finishes the LRA is automatically terminated and the `@Complete`
method will be invoked (if the LRA cancelled for any reason then  the `@Compensate`
method would have been triggered instead.

If you look at the implementation of `@Complete` callback you should notice that
it prints a count of how many times it has been notified. This output should be
visible in the terminal where you started the service.

The service also contains a JAX-RS method for reporting how many times it has
been notified:

<pre>
curl http://localhost:8081/cdi
</pre>

which should report

<pre>
1 completed and 0 compensated   
</pre>

## Recovering from failure

Inject a fault into the service causing it to halt:

<pre>
curl -X PUT -I http://localhost:8081/cdi?fault=haltcdiduring
</pre>

<pre>
2019-10-07 16:43:22,162 INFO  [stdout] (default task-2) 2 completions
2019-10-07 16:43:22,162 INFO  [stdout] (default task-2) injecting fault type HALT ...
2019-10-07 16:43:22,162 INFO  [stdout] (default task-2) HALTING
curl: (52) Empty reply from server
</pre>

Notice that the service printed HALTING and then the JVM exited. There should be
a pending record in the transaction logs which should appear under the following
directory:

<pre>
/tmp/txlogs/ShadowNoFileLockStore/defaultStore/StateManager/BasicAction/TwoPhaseCoordinator/LRA/
</pre>

Now restart the service and wait for recovery to complete the LRA and call the
service completion callback:

<pre>
java -Dthorntail.http.port=8081 -Dlra.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar target/lra-participant-example-thorntail.jar
</pre>

The recovery system can take a few minutes to recover the state or you can trigger
an immediate recovery scan using a curl request:

<pre>
curl http://localhost:8080/lra-recovery-coordinator/recovery
</pre>

This command will attempt to recover any pending LRAs If any participant involved
with the LRA is unavailable this request will print the id of the corresponding LRA.
But if the participant is available then it should return success allowing the LRA
to complete and you should see something similar to the following printed in the
service console:

<pre>
2018-10-14 17:38:51,831 INFO  [stdout] (default task-3) 1 completions
</pre>

Sometimes it can take more than one recovery pass to recover an LRA. If the
recovery command returns more than zero pending LRAs then you should rerun
the recover command. Note that if you run the example more than once without
waiting for recovery there will be more than one recovery record in the
transaction log storage.

Now you can confirm that the service did indeed complete by sending the query:

<pre>
curl  http://localhost:8081/cdi
</pre>

which should result in the following output:

<pre>
1 completed and 0 compensated
</pre>

Notice that the service itself is just an example so does not persist its
application state (ie its internal count of the number of completion notifications
is set to zero whenever the service starts).

Kill the service and coordinator in preparation for the next quickstart.

Remark: if the coordinator fails before the end phase begins then they will
be reported by a recovery scan but must be completed manually.

## Removing transaction logs

The default location where LRA logs are stored is `/tmp/txlogs/`. If there
are pending logs in need of recovery but you want to start afresh then you
may remove them manually:

<pre>
rm -rf /tmp/txlogs
</pre>

Note though that in a production system you would never want to manually
delete these logs since that would compromise the Atomicity guarantees
that the LRA specification provides.
