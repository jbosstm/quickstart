FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR lra-test-swarm.jar
ENV AB_ENABLED off
# to get more info about the service in console you can add switch
# -Dswarm.logging=TRACE
ENV JAVA_OPTIONS -Xmx512m -Dlra.http.host=flight-lra-coordinator -Dlra.http.port=8080

EXPOSE 8080

ADD target/lra-test-swarm.jar /deployments/
