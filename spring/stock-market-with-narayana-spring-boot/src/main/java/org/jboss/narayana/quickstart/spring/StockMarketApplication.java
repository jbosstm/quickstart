package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.narayana.DbcpXADataSourceWrapper;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DbcpXADataSourceWrapper.class)
public class StockMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMarketApplication.class, args);
    }
}