FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR lra-test-swarm.jar
ENV AB_ENABLED off
# to get more info about LRA processing in console you can add switch
#-Dswarm.logging=TRACE
ENV JAVA_OPTIONS -Xmx512m -Dlra.http.host=lra-coordinator-lra-minishift.192.168.99.100.nip.io -Dlra.http.port=80 -Dservice.http.host=trip-service-lra-minishift.192.168.99.100.nip.io -Dservice.http.port=80

EXPOSE 8080

ADD target/lra-test-swarm.jar /deployments/
