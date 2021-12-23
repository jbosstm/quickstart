FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR quarkus-run.jar
ENV AB_ENABLED off
# to get more info about the service in console you can add switch
# -Dquarkus.log.level=TRACE
ENV JAVA_OPTIONS -Xmx512m

EXPOSE 8080

ADD target/quarkus-app/quarkus-run.jar /deployments/
