FROM registry.access.redhat.com/ubi8/openjdk-21:1.18

ENV JAVA_APP_JAR lra-coordinator-runner.jar
ENV AB_ENABLED off
# to get more info about LRA processing in console you can add switch
# -quarkus.logging=TRACE
ENV JAVA_OPTIONS -Xmx512m -DObjectStoreEnvironmentBean.communicationStore.objectStoreDir=../../data -DObjectStoreEnvironmentBean.objectStoreDir=../../data -DObjectStoreEnvironmentBean.stateStore.objectStoreDir=../../data

EXPOSE 8080

ADD target/lra-coordinator-runner.jar /deployments/
