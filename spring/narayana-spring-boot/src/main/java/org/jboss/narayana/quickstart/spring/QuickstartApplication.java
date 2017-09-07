/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;

/**
 * Main Spring Boot application class.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootApplication
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
