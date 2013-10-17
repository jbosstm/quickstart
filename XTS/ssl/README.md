# How to configure Wildfly and XTS to use SSL

This example walks you through the steps required to setup two servers (client and server) that communicate via Web services over a secure connection.
The example show how this can be done for WS-Atomic Transaction, but the same applies for WS Business Activity.

*Note:* The example is split into several sections. Each section is named after the console tab in which you should run the commands.
For example, a section entitled "Tab 1" , is stating that the commands in that section should be done in the first console tab or window.


## Tab 1
Obtain Wildfly master and make two copies of the distribution. One for the client and one for the server.

    git clone https://github.com/wildfly/wildfly.git
    mvn clean install -DskipTests -f wildfly/pom.xml
    cp -r wildfly/build/target/wildfly-8.0.0.Beta2-SNAPSHOT/ wildfly-client
    cp -r wildfly/build/target/wildfly-8.0.0.Beta2-SNAPSHOT/ wildfly-server

Now clone the Wildfly quickstarts. We will be using an existing WS-AT example (wsat-simple) in this guide.

    git clone https://github.com/wildfly/quickstart.git
    cd quickstart/wsat-simple/

Now you need to create two aliased network interfaces, so that you can run two Wildfly servers on the same machine. You could also use port-offsetting.

On Linux (tested with Fedora):

    sudo ifconfig lo:2 127.0.0.2
    sudo ifconfig lo:3 127.0.0.3

On OSX:

    sudo ifconfig lo0 alias 127.0.0.2
    sudo ifconfig lo1 alias 127.0.0.3

Now use the following instructions to replace http urls with https urls, and change the destination IP.

in:

    ./src/main/webapp/WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl

Change:

    http://localhost:8080/jboss-as-wsat-simple/RestaurantServiceAT

To:

    https://127.0.0.3:8443/jboss-as-wsat-simple/RestaurantServiceAT

in:

    ./src/test/java/org/jboss/as/quickstarts/wsat/simple/Client.java

Change:

    http://localhost:8080/wsat-simple/RestaurantServiceAT?wsdl

To:

    https://127.0.0.3:8443/wsat-simple/RestaurantServiceAT?wsdl

in:
    ./src/main/java/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceATService.java

Change:

    @WebServiceClient(name = "RestaurantServiceATService", targetNamespace = "http://www.jboss.org/jboss-jdf/jboss-as-quickstart/wsat/simple/Restaurant")

To:

    @WebServiceClient(name = "RestaurantServiceATService", targetNamespace = "http://www.jboss.org/jboss-jdf/jboss-as-quickstart/wsat/simple/Restaurant", wsdlLocation = "WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl")


Now build the example.

    mvn clean install
    cd ../..




## Tab 2 (client)

Create a client configuration and configure it to use one of our aliased interfaces.

    cd wildfly-client/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sed -ie 's/127\.0\.0\.1/127\.0\.0\.2/g' standalone/configuration/standalone-xts.xml

Configure the keystore and export the public key.

    keytool -genkey -alias client -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass client -storepass client -dname "cn=client, ou=GSS, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias client -file client.cer -keypass client -storepass client


Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the server.keystore.

    cd wildfly-client
    ./bin/jboss-cli.sh --connect --controller=127.0.0.2:9990

    /core-service=management/security-realm=SSLRealm:add()
    /core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=./standalone/configuration/server.keystore, keystore-password=client, alias=client)
    /subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")


## Tab 2 (client)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.2}:8080/ws-c11/ActivationService"/>
to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.2}:8443/ws-c11/ActivationService"/>


## Tab 3 (server)
Create a server configuration and configure it to use one of our aliased interfaces.

    cd wildfly-server/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sed -ie 's/127\.0\.0\.1/127\.0\.0\.3/g' standalone/configuration/standalone-xts.xml

Configure the keystore and export the public key.

    keytool -genkey -alias server -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass server -storepass server -dname "cn=$HOSTNAME, ou=GSS, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias server -file server.cer -keypass server -storepass server

Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the server.keystore.

    ./bin/jboss-cli.sh --connect --controller=127.0.0.3:9990

    /core-service=management/security-realm=SSLRealm:add()
    /core-service=management/security-realm=SSLRealm/server-identity=ssl:add(keystore-path=./standalone/configuration/server.keystore, keystore-password=server, alias=server)
    /subsystem=undertow/server=default-server/https-listener=https:add(socket-binding="https", security-realm="SSLRealm")


## Tab 3 (server)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.3}:8080/ws-c11/ActivationService"/>

to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.3}:8443/ws-c11/ActivationService"/>


## Tab 2 (client)
Import the server's public key into the client's truststore

    keytool -import -noprompt -v -trustcacerts -alias server -file ../wildfly-server/server.cer -keystore ./standalone/configuration/server.keystore -keypass client -storepass client


## Tab 3 (server)
Import the client's public key into the server's truststore

    keytool -import -noprompt -v -trustcacerts -alias client -file ../wildfly-client/client.cer -keystore ./standalone/configuration/server.keystore -keypass server -storepass server


## Tab 1
Deploy the example application to both servers.

    cp quickstart/wsat-simple/target/jboss-as-wsat-simple.war ./wildfly-client/standalone/deployments/
    cp quickstart/wsat-simple/target/jboss-as-wsat-simple.war ./wildfly-server/standalone/deployments/


## Tab 3 (server)
Start the server.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=./standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=server -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore=./standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=server


## Tab 2 (client)
Start the client.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=./standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=client -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore=./standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=client


Now visit the servlet on the client, to see if it works: http://127.0.0.2:8080/jboss-as-wsat-simple/WSATSimpleServletClient. You should see no exceptions in either server log; in which case you now have XTS and the application configured to use SSL.
