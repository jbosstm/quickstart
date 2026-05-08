package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import dev.snowdrop.boot.narayana.autoconfigure.NarayanaAutoConfiguration;

@SpringBootApplication
@Import(NarayanaAutoConfiguration.class)
public class StockMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMarketApplication.class, args);
    }
}
