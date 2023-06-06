package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.narayana.DbcpXADataSourceWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.io.Closeable;

/**
 * Main Spring Boot application class.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootApplication
@Import(DbcpXADataSourceWrapper.class)
public class QuickstartApplication {

    public static final Object TO_WAIT = new Object();

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        } else if (args.length == 1 && !args[0].toUpperCase().equals(CompleteAction.RECOVERY.name())) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        }

        ApplicationContext context = SpringApplication.run(QuickstartApplication.class, args);
        QuickstartService quickstartService = context.getBean(QuickstartService.class);

        switch (CompleteAction.valueOf(args[0].toUpperCase())) {
            case COMMIT:
                synchronized (TO_WAIT) {
                    quickstartService.demonstrateCommit(args[1]);
                    TO_WAIT.wait();
                }
                break;
            case ROLLBACK:
                quickstartService.demonstrateRollback(args[1]);
                break;
            case CRASH:
                quickstartService.demonstrateCrash(args[1]);
                break;
            case RECOVERY:
                quickstartService.demonstrateRecovery();
        }

        ((Closeable) context).close();
    }

    public enum CompleteAction {
        COMMIT, ROLLBACK, CRASH, RECOVERY
    }

}