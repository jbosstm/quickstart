package org.jboss.narayana.quickstart.spring;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.jboss.narayana.quickstart.spring.service.ExampleService;
import org.jboss.narayana.quickstart.spring.xa.DummyXAResource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class Launch {
    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(Launch.class.getPackage().getName())) {
            ExampleService service = context.getBean(ExampleService.class);
            if (args.length == 1) {
                RecoveryManagerService recoveryManagerService = context.getBean(RecoveryManagerService.class);
                if (args[0].equals("-f")) {
                    System.out.println("Generate something to recovery ...");
                    service.testRecovery();
                } else if (args[0].equals("-r")) {
                    System.out.println("start the recovery manager");
                    recoveryManagerService.start();

                    System.out.println("recovery manager scan ...");
                    while (DummyXAResource.getCommitRequests() == 0) {
                        Thread.sleep(1000);
                    }

                    System.out.println("stop the recovery manager");
                    recoveryManagerService.stop();
                } else if (args[0].equals("-c")) {
                    service.checkRecord();
                }
            } else {
                service.testCommit();
                service.checkRecord();
            }

            service.shutdownDatabase();
        }
        TxControl.disable(true);
        TransactionReaper.terminate(true);
    }
}