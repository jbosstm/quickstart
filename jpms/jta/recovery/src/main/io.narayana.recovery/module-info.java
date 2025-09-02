
module io.narayana.recovery {
    // narayana is not modular so the compiler will generate a warning:
    // https://stackoverflow.com/questions/46501047/what-does-required-filename-based-automodules-detected-warning-mean
    requires narayana.jta;
    requires org.jboss.logging;
    requires jakarta.transaction;
    requires jakarta.annotation;
    requires java.transaction.xa;
    requires jboss.transaction.spi;
    requires java.sql;
    requires io.narayana.config;

    exports io.narayana.recovery;
}
