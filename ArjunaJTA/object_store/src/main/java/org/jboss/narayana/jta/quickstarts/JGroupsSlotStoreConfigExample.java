/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.jta.quickstarts;

import jakarta.transaction.UserTransaction;

/**
 * Example showing properties-file-based configuration of a JGroups
 * ReplCache-backed object store. All bean settings live in
 * {@code jgroups-jbossts-properties.xml} on the classpath.
 */
public class JGroupsSlotStoreConfigExample {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "jgroups-jbossts-properties.xml");

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        System.exit(0);
    }
}
