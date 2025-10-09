package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import dev.snowdrop.boot.narayana.autoconfigure.NarayanaAutoConfiguration;

/**
 * Main Spring Boot application class.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootApplication
@Import(NarayanaAutoConfiguration.class)
public class QuickstartApplication {

    public static final Object TO_WAIT = new Object();

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        } else if (args.length == 1 && !args[0].toUpperCase().equals(CompleteAction.RECOVERY.name())) {
            throw new IllegalArgumentException("Invalid arguments provided. See README.md for usage examples");
        }

        ConfigurableApplicationContext context = SpringApplication.run(QuickstartApplication.class, args);
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
        context.close();
    }

    public enum CompleteAction {

        COMMIT("COMMIT"), ROLLBACK("ROLLBACK"), CRASH("CRASH"), RECOVERY("RECOVERY");

        public final String label;

        private CompleteAction(String label) {
            this.label = label;
        }
    }

}