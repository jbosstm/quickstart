/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jbossts.txbridge.demotest;

import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.name;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.jboss.arquillian.ajocado.Ajocado.xp;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.NameLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.zip.ZipFile;

/**
 * Basic tests for tx bridge demo application.
 *
 * @author istudens@redhat.com
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TxBridgeDemoTest {
    private static final Logger log = Logger.getLogger(TxBridgeDemoTest.class);

    private static final String XTS_DEMO_DIR = "../../XTS/xts-install/demo/build/";
    private static final String XTS_DEMO_ARCHIVE = "xts-demo.ear";

    private static final String TXBRIDGE_DEMO_DIR = "../demo/build/";
    private static final String TXBRIDGE_DEMO_SERVICE_ARCHIVE = "txbridge-demo-service.jar";
    private static final String TXBRIDGE_DEMO_CLIENT_ARCHIVE = "txbridge-demo-client.war";

    private static final String RESULT_TITLE = "Transaction Result";
    private static final String TRANSACTION_FINISHED = "Transaction finished OK";

    private static final String PARENT_TX_TYPE_AT = "AtomicTransaction";
    private static final String PARENT_TX_TYPE_JTA = "JTA";


    // load ajocado driver
    @Drone
    AjaxSelenium driver;


    protected NameLocator TX_TYPE_FIELD = name("txType");
    protected NameLocator SEATS_FIELD = name("seats");

    protected NameLocator SUBMIT_BUTTON = name("submit");

    protected XPathLocator RESULT_TABLE_TITLE_XP = xp("//span[@class='result_title']");
    protected XPathLocator RESULT_TABLE_CONTENT_XP = xp("//span[@class='result']");


    @Deployment(name = XTS_DEMO_ARCHIVE, testable = false, order = 1)
    public static Archive<?> createXTSDemoArchive() throws Exception {
        Archive<?> archive = ShrinkWrap.create(ZipImporter.class, XTS_DEMO_ARCHIVE)
                .importFrom(new ZipFile(XTS_DEMO_DIR + XTS_DEMO_ARCHIVE)).as(EnterpriseArchive.class);
        return archive;
    }

    @Deployment(name = TXBRIDGE_DEMO_SERVICE_ARCHIVE, testable = false, order = 2)
    public static Archive<?> createTxBridgeDemoServiceArchive() throws Exception {
        Archive<?> archive = ShrinkWrap.create(ZipImporter.class, TXBRIDGE_DEMO_SERVICE_ARCHIVE)
                .importFrom(new ZipFile(TXBRIDGE_DEMO_DIR + TXBRIDGE_DEMO_SERVICE_ARCHIVE)).as(JavaArchive.class);
        return archive;
    }

    @Deployment(name = TXBRIDGE_DEMO_CLIENT_ARCHIVE, testable = false, order = 3)
    public static Archive<?> createTxBridgeDemoClientArchive() throws Exception {
        Archive<?> archive = ShrinkWrap.create(ZipImporter.class, TXBRIDGE_DEMO_CLIENT_ARCHIVE)
                .importFrom(new ZipFile(TXBRIDGE_DEMO_DIR + TXBRIDGE_DEMO_CLIENT_ARCHIVE)).as(WebArchive.class);
        return archive;
    }

    @Test  @OperateOnDeployment(TXBRIDGE_DEMO_CLIENT_ARCHIVE)
    public void testAT(@ArquillianResource URL contextPath) throws Exception {
        testReservation(PARENT_TX_TYPE_AT, "2", contextPath);
    }

    @Test  @OperateOnDeployment(TXBRIDGE_DEMO_CLIENT_ARCHIVE)
    public void testJTA(@ArquillianResource URL contextPath) throws Exception {
        testReservation(PARENT_TX_TYPE_JTA, "3", contextPath);
    }

    protected void testReservation(String txType, String seats, URL contextPath) throws Exception {
        log.info("contextPath = " + contextPath);
        driver.open(contextPath);

        waitModel.until(elementPresent.locator(TX_TYPE_FIELD));
        log.info("driver.getTitle() = " + driver.getTitle());

        driver.type(TX_TYPE_FIELD, txType);
        driver.type(SEATS_FIELD, seats);

        waitForHttp(driver).click(SUBMIT_BUTTON);

        String resultTableTitle = driver.getText(RESULT_TABLE_TITLE_XP);
        log.info("resultTableTitle = " + resultTableTitle);
        Assert.assertTrue("Page does not contain any results!", resultTableTitle.contains(RESULT_TITLE));

        String resultTableContent = driver.getText(RESULT_TABLE_CONTENT_XP);
        log.info("resultTableContent = " + resultTableContent);
        Assert.assertTrue("Transaction failed with: " + resultTableContent, resultTableContent.contains(TRANSACTION_FINISHED));
    }

}
