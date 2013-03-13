/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.narayana.quickstart.jca.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.narayana.quickstart.jca.model.CustomerDAO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractTest {

    private static final String JDBC_RAR_FILE_PATH = "src/main/resources/jdbc-xa.rar";

    private static final String DATA_SOURCE_FILE_PATH = "src/test/resources/h2-xa-ds.xml";

    private static final String DATA_SOURCE_JNDI = "java:/H2XADS";

    private static Embedded EMBEDDED;

    protected CustomerDAO customerDAO;

    @BeforeClass
    public static void beforeClass() throws Throwable {
        EMBEDDED = EmbeddedFactory.create();
        EMBEDDED.startup();
        EMBEDDED.deploy(getURL(JDBC_RAR_FILE_PATH));
        EMBEDDED.deploy(getURL(DATA_SOURCE_FILE_PATH));
    }

    @AfterClass
    public static void afterClass() throws Throwable {
        EMBEDDED.undeploy(getURL(DATA_SOURCE_FILE_PATH));
        EMBEDDED.undeploy(getURL(JDBC_RAR_FILE_PATH));
        EMBEDDED.shutdown();
    }

    @Before
    public void before() throws Exception {
        customerDAO = new CustomerDAO(DATA_SOURCE_JNDI);
        customerDAO.clear();
    }

    @After
    public void after() throws SQLException {
        customerDAO.clear();
    }

    private static URL getURL(final String path) throws MalformedURLException {
        final File f = new File(path);

        return f.toURI().toURL();
    }
}
