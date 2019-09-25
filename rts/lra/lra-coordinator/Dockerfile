FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR lra-coordinator-runner.jar
ENV AB_ENABLED off
# to get more info about LRA processing in console you can add switch
# -quarkus.logging=TRACE
ENV JAVA_OPTIONS -Xmx512m -DObjectStoreEnvironmentBean.communicationStore.objectStoreDir=../../data -DObjectStoreEnvironmentBean.objectStoreDir=../../data -DObjectStoreEnvironmentBean.stateStore.objectStoreDir=../../data

EXPOSE 8080

ADD target/lra-coordinator-runner.jar /deployments/
