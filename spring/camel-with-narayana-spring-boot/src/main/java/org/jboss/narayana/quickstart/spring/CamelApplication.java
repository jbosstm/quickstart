package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.narayana.DbcpXADataSourceWrapper;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:zfeng@redhat.com>Amos Feng</a>
 */
@SpringBootApplication
@Import(DbcpXADataSourceWrapper.class)
public class CamelApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelApplication.class, args);
	}

}