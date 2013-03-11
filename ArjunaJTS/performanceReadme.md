cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-8.0.0.Alpha1-SNAPSHOT/ jts/server1
cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-8.0.0.Alpha1-SNAPSHOT/ jts/server2
cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-8.0.0.Alpha1-SNAPSHOT/ jta/server3

cp server1-standalone-full.xml jts/server1/standalone/configuration/standalone-full.xml
cp server2-standalone-full.xml jts/server2/standalone/configuration/standalone-full.xml

# to run with asynchronous prepare:
cp server1-async-prepare-standalone-full.xml jts/server1/standalone/configuration/standalone-full.xml
cp server2-async-prepare-standalone-full.xml jts/server2/standalone/configuration/standalone-full.xml

./server1/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=000
./server2/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=100
./server3/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=200

mvn clean install jboss-as:deploy

curl http://localhost:8080/jboss-as-jts-application-component-1/addCustomer.jsf?name=100
curl http://localhost:8280/jboss-as-jta/addCustomer.jsf?name=100
