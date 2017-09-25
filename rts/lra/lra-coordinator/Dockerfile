FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR lra-coordinator-swarm.jar
ENV AB_ENABLED off
# to get more info about LRA processing in console you can add switch
# -Dswarm.logging=TRACE
ENV JAVA_OPTIONS -Xmx512m -Dswarm.transactions.object-store-path=../../data

EXPOSE 8080

ADD target/lra-coordinator-swarm.jar /deployments/