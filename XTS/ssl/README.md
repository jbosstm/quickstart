# How to configure JBoss EAP and XTS to use SSL

This example walks you through the steps required to setup two servers (client and server) that communicate via Web services over a secure connection.
The example show how this can be done for WS-Atomic Transaction, but the same applies for WS Business Activity.

*Note:* The example is split into several sections. Each section is named after the console tab in which you should run the commands.
For example, a section entitled "Tab 1" , is stating that the commands in that section should be done in the first console tab or window.


## Tab 1
Obtain JBoss EAP 6.1 and make two copies of the distribution. One for the client and one for the server.

    unzip ~/Downloads/jboss-eap-6.1.0.zip
    mv jboss-eap-6.1 jboss-eap-6.1-client
    cp -r jboss-eap-6.1-client jboss-eap-6.1-server

Now clone the JBoss Developer quickstarts. We will be using an existing WS-AT example (wsat-simple) in this quide.

    git clone git://github.com/jboss-jdf/jboss-as-quickstart.git
    cd jboss-as-quickstart/wsat-simple/

Now you need to create two aliased network interfaces, so that you can run two EAP servers on the same machine. You could also use port-offsetting.

On Linux (tested with Fedora):

    sudo ifconfig lo:2 127.0.0.2
    sudo ifconfig lo:3 127.0.0.3

On OSX:

    sudo ifconfig lo0 alias 127.0.0.2
    sudo ifconfig lo0 alias 127.0.0.3

Now use the following instructions to replace http urls with https urls, and change the destination IP.

in:

    ./src/main/webapp/WEB-INF/classes/org/jboss/as/quickstarts/wsat/simple/jaxws/RestaurantServiceAT.wsdl

Change:

    http://localhost:8080/jboss-wsat-simple/RestaurantServiceAT

To:

    https://127.0.0.3:8443/jboss-wsat-simple/RestaurantServiceAT

in:

        ./src/test/java/org/jboss/as/quickstarts/wsat/simple/Client.java

Change:

        http://localhost:8080/jboss-wsat-simple/RestaurantServiceAT?wsdl

To:

        https://127.0.0.3:8443/jboss-wsat-simple/RestaurantServiceAT?wsdl


Now build the example.

    mvn clean install
    cd ../..




## Tab 2 (client)

Create a client configuration and configure it to use one of our aliased interfaces.

    cd jboss-eap-6.1-client/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sed -ie 's/127\.0\.0\.1/127\.0\.0\.2/g' standalone/configuration/standalone-xts.xml

Configure the keystore and export the public key.

    keytool -genkey -alias client -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass client -storepass client -dname "cn=client, ou=GSS, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias client -file client.cer -keypass client -storepass client


Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the server.keystore.

    cd jboss-eap-6.1-client
    ./bin/jboss-cli.sh --connect --controller=127.0.0.2:9999

    /subsystem=web/connector=https/:add(socket-binding=https,scheme=https,protocol=HTTP/1.1,secure=true)
    #Notice the hard coded keystore location
    /subsystem=web/connector=https/ssl=configuration:add(name=https,certificate-key-file=/opt/ssl/jboss-eap-6.1-client/standalone/configuration/server.keystore,password=client, key-alias=client


## Tab 2 (client)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.1}:8080/ws-c11/ActivationService"/>
to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.2}:8443/ws-c11/ActivationService"/>


## Tab 3 (server)
Create a server configuration and configure it to use one of our aliased interfaces.

    cd jboss-eap-6.1-server/
    cp docs/examples/configs/standalone-xts.xml standalone/configuration/
    sed -ie 's/127\.0\.0\.1/127\.0\.0\.3/g' standalone/configuration/standalone-xts.xml

Configure the keystore and export the public key.

    keytool -genkey -alias server -keyalg RSA -keysize 1024 -keystore ./standalone/configuration/server.keystore -validity 3650 -keypass server -storepass server -dname "cn=$HOSTNAME, ou=GSS, o=Red Hat, l=Raleigh, st=NC, c=US"
    keytool -export -keystore ./standalone/configuration/server.keystore -alias server -file server.cer -keypass server -storepass server

Start the server, ready to connect the JBoss CLI tool.

    ./bin/standalone.sh -c standalone-xts.xml


## Tab 4 (admin)
Connect to the client server and configure SSL. Remember to change the commands to refer to the location where you created the server.keystore.

    ./bin/jboss-cli.sh --connect --controller=127.0.0.3:9999

    /subsystem=web/connector=https/:add(socket-binding=https,scheme=https,protocol=HTTP/1.1,secure=true)
    #Notice the hard coded keystore location
    /subsystem=web/connector=https/ssl=configuration:add(name=https,certificate-key-file=/opt/ssl/jboss-eap-6.1-server/standalone/configuration/server.keystore,password=server, key-alias=server

## Tab 3 (server)
kill the server and then edit the config, to make sure the client's XTS coordinator is used.

    vi standalone/configuration/standalone-xts.xml

change:

    <xts-environment url="http://${jboss.bind.address:127.0.0.1}:8080/ws-c11/ActivationService"/>

to:

    <xts-environment url="https://${jboss.bind.address:127.0.0.1}:8443/ws-c11/ActivationService"/>


## Tab 2 (client)
Import the server's public key into the client's truststore

    keytool -import -noprompt -v -trustcacerts -alias server -file ../jboss-eap-6.1-server/server.cer -keystore ./standalone/configuration/server.keystore -keypass client -storepass client


## Tab 3 (server)
Import the client's public key into the server's truststore

    keytool -import -noprompt -v -trustcacerts -alias client -file ../jboss-eap-6.1-client/client.cer -keystore ./standalone/configuration/server.keystore    -keypass server -storepass server


## Tab 1
Deploy the example application to both servers.

    cp jboss-as-quickstart/wsat-simple/target/jboss-wsat-simple.war ./jboss-eap-6.1-client/standalone/deployments/
    cp jboss-as-quickstart/wsat-simple/target/jboss-wsat-simple.war ./jboss-eap-6.1-server/standalone/deployments/


## Tab 4 (server)
Start the server. Remember to change the following command to set your location for -Djavax.net.ssl.keyStore and -Djavax.net.ssl.trustStore.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=/opt/ssl/jboss-eap-6.1-server/standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=server -Dorg.jboss.security.ignoreHttpsHost=true    -Djavax.net.ssl.keyStore=/opt/ssl/jboss-eap-6.1-server/standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=server

Check the server booted without errors, then visit https://127.0.0.3:8443/jboss-wsat-simple/RestaurantServiceAT?wsdl. Check the wsdl displays and that the `soap:address#location"`is the address of the secure endpoint


## Tab 3 (client)
Start the client. Remember to change the following command to set your location for -Djavax.net.ssl.keyStore and -Djavax.net.ssl.trustStore.

    ./bin/standalone.sh -c standalone-xts.xml -Djavax.net.ssl.trustStore=/opt/ssl/jboss-eap-6.1-client/standalone/configuration/server.keystore -Djavax.net.ssl.trustStorePassword=client -Dorg.jboss.security.ignoreHttpsHost=true -Djavax.net.ssl.keyStore=/opt/ssl/jboss-eap-6.1-client/standalone/configuration/server.keystore -Djavax.net.ssl.keyStorePassword=client

Check the server booted without errors, then visit https://127.0.0.3:8443/jboss-wsat-simple/RestaurantServiceAT?wsdl. Check the wsdl displays and that the `soap:address#location` is the address of the secure endpoint.

Now visit the servlet on the client, to see if it works: http://127.0.0.2:8080/jboss-wsat-simple/WSATSimpleServletClient. You should see no exceptions in either server log; in which case you now
have XTS and the application configured to use SSL.