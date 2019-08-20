# MicroProfile LRA Examples

[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) is a specification of an API that
enables loosely coupled services to coordinate long running activities in such a way as to
guarantee a globally consistent outcome without the need to take long duration locks on data.

These examples take a normal WAR and wraps them into -thorntail runnable jars.

Each project uses maven war packaging in the `pom.xml`

    <packaging>war</packaging>

amd adds a `<plugin>` to configure `thorntail-maven-plugin` to
create the runnable `.jar`:

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

Two quickstarts are included:

## cdi-participant-with-coordinator

[Shows a service which registers with an embedded LRA coordinator using CDI annotations](#cdi-participant-with-coordinator/README.md) 

## cdi-participant

[Shows a service which registers with an external LRA coordinator using CDI annotations](#cdi-participant/README.md)

## Running an external LRA coordinator

The maven module `lra-coordinator` contains a project for building a thorntail jar that runs a
standalone LRA coordinator. The port on which the coordinator listens for requests is defined
by the system property `thorntail.http.port`. The coordinator relies on persistent storage for
creating logs in order to be able to recover from failures. Normally thorntail uses a temporary
directory for its logs which is not useful in recovery situations since the storage
so you need to overide this default using the following system property:

> -Dthorntail.transactions.object-store-path=../txlogs

So, assuming you are in the lra-examples directory, to start a coordinator type:

> java -Dthorntail.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar lra-coordinator/target/lra-coordinator-thorntail.jar

## Running the cdi-participant-with-coordinator example

This example includes a dependency on the org.jboss.narayana.rts:lra-coordinator maven artefact
in the project pom:

```
        <dependency>
            <groupId>org.jboss.narayana.rts</groupId>
            <artifactId>lra-coordinator</artifactId>
            <version>${project.version}</version>
            <type>war</type>
        </dependency>
```

The JAX-RS service code is identical to the `cdi-participant` example. In fact the service code
is pulled in via the pom dependency on that example:

```
        <dependency>
            <groupId>org.jboss.narayana.quickstart.rts.lra</groupId>
            <artifactId>examples-microprofile-lra-cdi-participant</artifactId>
            <version>${project.version}</version>
            <type>war</type>
        </dependency>
```

Including the artefact will result in a coordinator being co-located with the service.

### The successful path

Start the service on port 8080. Since the example uses an embedded coordinator, make sure you use
non temporary storage for the logs using the `thorntail.transactions.object-store-path` system property.
Also set the system property, `thorntail.http.port`,  which defines the port the coordinator on which
it will accept requests (since the coordinator and service are co-located you must ensure that port
for the service and the coordinator are the same).

> java -Dthorntail.http.port=8080 -Dlra.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar cdi-participant-with-coordinator/target/lra-participant-example-thorntail.jar

In another terminal send a request to the service using curl:

> curl -X PUT -I http://localhost:8080/cdi

<pre>
HTTP/1.1 204 No Content
Date: Sun, 14 Oct 2018 10:51:15 GMT
</pre>

This invokes a service method which is annotated with `@LRA(LRA.Type.REQUIRED)`. This annotation will
result in an LRA being started before the method called. Since the service resource is annotated with
`@Complete` and `@Compensate` annotations the resource will be also be enlisted in the new LRA before
the method is invoked.

When the method finishes the LRA is automatically terminated and the `@Complete` method will be invoked.
If you look at the implementation of this callback you should notice that it prints a count of how many
times it has been notified. This output should be visible in the terminal where you started the service.

You can also ask the service how many times it has been notified:

> curl http://localhost:8080/cdi

which should report

<pre>
1 completed and 0 compensated   
</pre>

### Recovery from failure

Inject a fault into the service causing it to halt:

> curl -X PUT -I http://localhost:8080/cdi?fault=haltcdiduring

<pre>
curl: (52) Empty reply from server
</pre>

and you should notice that service console prints:

<pre>
2018-10-14 12:09:19,451 INFO  [stdout] (default task-11) injecting fault type HALT ..
</pre>

and there should be a pending record in the transaction logs which should appear under the
following directory:

> /tmp/txlogs/ShadowNoFileLockStore/defaultStore/StateManager/BasicAction/TwoPhaseCoordinator/LRA/

Now restart the service and wait for recovery to complete the LRA and call the service completion callback:

> java -Dthorntail.http.port=8080 -Dlra.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar target/lra-participant-example-thorntail.jar

The recovery system can take a few minutes to recover the state or you can trigger an immediate
recovery scan using a curl request:

> curl http://localhost:8080/lra-recovery-coordinator/recovery

This command will attempt to recover any pending LRA's. If any participant involved with the LRA is
unavailable this request will print the id of the corresponding LRA. But if the participant is
available then it should return success allowing the LRA to complete and you should see something
similar to the following printed in the service console:

> 2018-10-14 17:38:51,831 INFO  [stdout] (default task-3) 1 completions

Sometimes it can take more than one recovery pass to recover an LRA. If the recovery command returns
more than zero pending LRA's then you should rerun the recover command.

And you can confirm that the service did indeed complete by sending the query:

> curl  http://localhost:8080/cdi

which should result in the following output:

<pre>
1 completed and 0 compensated
</pre>

## Running the cdi-participant example

This example is similar to the cdi-participant-with-coordinator example except that it
uses an external coordinator.

The example requires the Eclipse MicroProfile LRA annotations and the name of the JAX-RS
header that exposes the id of any LRA context during JAX-RS invocations:


```
        <dependency>
            <groupId>io.narayana.microprofile.lra</groupId>
            <artifactId>microprofile-lra-api</artifactId>
            <version>${version.microprofile.lra}</version>
        </dependency>
```

The spec implementation of the LRA annotations is provided by the use of JAX-RS filters which
are activated by including the following dependency:

```
        <dependency>
            <groupId>org.jboss.narayana.rts</groupId>
            <artifactId>narayana-lra</artifactId>
            <version>${project.version}</version>
        </dependency>
```

Start the coordinator as [explained previously](#running-an-external-lra-coordinator) on port 8080:

> java -Dthorntail.http.port=8080 -Dthorntail.transactions.object-store-path=../txlogs -jar lra-coordinator/target/lra-coordinator-thorntail.jar

Start the service on port 8082 (using the thorntail.http.port system property) and tell the service which
port to use for the coordinator (using the lra.http.port system property):

> java -Dthorntail.http.port=8082 -Dlra.http.port=8080 -jar cdi-participant/target/lra-participant-example-thorntail.jar

Notice this time you did not need to specify the coordinator log directory because the coordinator
is now running separately from the service.

Test the service (running on port 8082):

> curl -X PUT -I http://localhost:8082/cdi

Verify that service was part of the LRA by either looking at the service console or by asking
the service if it was notified when the LRA was completing:

> curl http://localhost:8082/cdi

Recovery can be tested in the same way as before:

> curl -X PUT -I http://localhost:8082/cdi?fault=haltcdiduring

Restart the service:

> java -Dthorntail.http.port=8082 -Dlra.http.port=8080 -jar cdi-participant/target/lra-participant-example-thorntail.jar

When it is ready either wait for recovery or manually trigger a recovery scan (via the
coordinator running on port 8080):

> curl http://localhost:8080/lra-recovery-coordinator/recovery

Validate the service was asked to complete by either looking at the service console or by running
the query:

> curl  http://localhost:8082/cdi

Kill the service in preparation for the next quickstart.
Manually terminate the coordinator.

Remark: if the coordinator fails before the end phase begins then they will be reported by
a recovery scan but must be completed manually.
