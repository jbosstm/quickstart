
// TODO use the module name convention io.narayana.demo
//  bit I can't figure out how to create an executable jar with dots in the module name
//  (see the maven-jar-plugin config in the pom where we set the manifest mainClass
module io.narayana.demo {
    requires java.sql;
    requires io.narayana.config;
    requires io.narayana.recovery;
    requires io.narayana.txuser;
    requires jakarta.transaction;
    requires narayana.jta;
    requires org.apache.logging.log4j;
}
