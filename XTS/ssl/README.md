# How to configure WildFly and XTS to use SSL

This example walks you through the steps required to setup two servers (client and server) that communicate via Web services over a secure connection.
The example show how this can be done for WS-Atomic Transaction, but the same applies for WS Business Activity.

*Note:* The example is split into several sections. Each section is named after the console tab in which you should run the commands.
For example, a section entitled "Tab 1" , is stating that the commands in that section should be done in the first console tab or window.


## Tab 1

Obtain WildFly master and make two copies of the distribution. One for the client and one for the server.

    git clone https://github.com/wildfly/wildfly.git
    mvn clean install -DskipTests -f wildfly/pom.xml
    cp -r wildfly/build/target/wildfly-*-SNAPSHOT/ wildfly-client
    cp -r wildfly/build/target/wildfly-*-SNAPSHOT/ wildfly-server

Now clone the WildFly quickstarts. We will be using an existing WS-AT example (wsat-simple) in this guide.

    git clone https://github.com/wildfly/quickstart.git
    cd quickstart/wsat-simple/

For being possible to run two WildFly servers on the local machines will use port-offsetting
for each one binding different ports.

We need to make few adjustments in code of the `wsat-simple` to count with the port
offsets and with usage of HTTPS.

| In file  | Text to find | To change to
| --- | --- | --- |
| ./src/main/webapp/WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl  | http://localhost:8080/jboss-as-wsat-simple/RestaurantServiceAT  | https://localhost:8643/jboss-as-wsat-simple/RestaurantServiceAT |
| ./src/test/java/org/jboss/as/quickstarts/wsat/simple/Client.java | http://localhost:8080/wsat-simple/RestaurantServiceAT?wsdl | https://localhost:8643/wsat-simple/RestaurantServiceAT?wsdl |

The `wsat-simple` war archive has to be built with dependency to `org.jboss.xts` module.
If this is not configured in `pom.xml` already we need to add maven war plugin configuration under `<build>` element.

```xml
  <build>
      <!-- Set the name of the WAR, used as the context root when the app is deployed. -->
      <finalName>${project.artifactId}</finalName>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-war-plugin</artifactId>
              <configuration>
                  <archive>
                      <manifest/>
                      <manifestEntries>
                          <Dependencies>org.jboss.xts</Dependencies>
                      </manifestEntries>
                  </archive>
              </configuration>
          </plugin>
      </plugins>
  </build>
```


Now build the example.

    mvn clean install
    cd ../..


## Tab 2 (client)

Use `standalone-xts.xml` at the client and configure it to with port offset.

    cd wildfly-client/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/

Configure the keystore and export the public key.

    keytool -genkey -alias client -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass client -storepass client -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias client -file client.cer -keypass client -storepass client


Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the `server.keystore`.

    cd wildfly-client
    ./bin/jboss-cli.sh --connect

    /core-service=management/security-realm=SSLRealm:add()
    /core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=./standalone/configuration/server.keystore, keystore-password=client, alias=client)
    /subsystem=undertow/server=default-server/https-listener=https:remove()
    :reload()
    /subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")


## Tab 2 (client)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.1}:8080/ws-c11/ActivationService"/>
to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.4}:8543/ws-c11/ActivationService"/>


## Tab 3 (server)
Create a server configuration and configure it to use one of our aliased interfaces.

    cd wildfly-server/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/

Configure the keystore and export the public key.

    keytool -genkey -alias server -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass server -storepass server -dname "cn=$HOSTNAME, ou=jbossdev, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias server -file server.cer -keypass server -storepass server

Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the server.keystore.

    ./bin/jboss-cli.sh --connect

    /core-service=management/security-realm=SSLRealm:add()
    /core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=./standalone/configuration/server.keystore, keystore-password=server, alias=server)
    /subsystem=undertow/server=default-server/https-listener=https:remove()
    :reload()
    /subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")


## Tab 3 (server)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.1}:8080/ws-c11/ActivationService"/>

to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.1}:8643/ws-c11/ActivationService"/>


## Tab 2 (client)
Import the server's public key into the client's truststore

    keytool -import -noprompt -v -trustcacerts -alias server -file ../wildfly-server/server.cer -keystore ./standalone/configuration/server.keystore -keypass client -storepass client


## Tab 3 (server)
Import the client's public key into the server's truststore

    keytool -import -noprompt -v -trustcacerts -alias client -file ../wildfly-client/client.cer -keystore ./standalone/configuration/server.keystore -keypass server -storepass server


## Tab 1
Deploy the example application to both servers.

    cp quickstart/wsat-simple/target/wsat-simple.war ./wildfly-client/standalone/deployments/
    cp quickstart/wsat-simple/target/wsat-simple.war ./wildfly-server/standalone/deployments/


## Tab 3 (server)
Start the server.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=./standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=server -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore=./standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=server -Dcxf.tls-client.disableCNCheck=true -Djboss.socket.binding.port-offset=200


## Tab 2 (client)
Start the client.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=./standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=client -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore=./standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=client -Dcxf.tls-client.disableCNCheck=true -Djboss.socket.binding.port-offset=100


Now visit the servlet on the client, to see if it works: http://127.0.0.1:8180/WSATSimpleServletClient. You should see no exceptions in either server log; in which case you now have XTS and the application configured to use SSL.
