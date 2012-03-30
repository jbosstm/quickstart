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
package org.jboss.jbossts.xts.demotest;

import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.name;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.jboss.arquillian.ajocado.Ajocado.xp;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.NameLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.zip.ZipFile;

/**
 * Basic tests for XTS demo application.
 *
 * @author istudens@redhat.com
 */
@RunWith(Arquillian.class)
@RunAsClient
public class XTSDemoTest {
    private static final Logger log = Logger.getLogger(XTSDemoTest.class);

    private static final String XTS_DEMO_DIR = "../xts-install/demo/build/";
    private static final String XTS_DEMO_ARCHIVE = "xts-demo.ear";
    private static final String DEMO_APP_CONTEXT = "xts-demo";

    private static final String RESULT_TITLE = "Transaction Result";
    private static final String TRANSACTION_FINISHED = "Transaction finished OK";

    private static final String TX_TYPE_AT = "AtomicTransaction";
    private static final String TX_TYPE_BA = "BusinessActivity";


    // load ajocado driver
    @Drone
    AjaxSelenium driver;

    // Load context path to the test
    @ArquillianResource
    URL contextPath;


    protected NameLocator TX_TYPE_FIELD = name("txType");
    protected NameLocator RESTAURANT_FIELD = name("restaurant");
    protected NameLocator THEATER_CIRCLE_FIELD = name("theatrecirclecount");
    protected NameLocator THEATER_STALLS_FIELD = name("theatrestallscount");
    protected NameLocator THEATER_BALCONY_FIELD = name("theatrebalconycount");
    protected NameLocator TAXI_FIELD = name("taxi");

    protected NameLocator SUBMIT_BUTTON = name("submit");

    protected XPathLocator RESULT_TABLE_TITLE_XP = xp("//div[@class='result_title']");
    protected XPathLocator RESULT_TABLE_CONTENT_XP = xp("//div[@class='result']");


    @Deployment(name = XTS_DEMO_ARCHIVE, testable = false)
    public static Archive<?> createTestArchive() throws Exception {
        Archive<?> archive = ShrinkWrap.create(ZipImporter.class, XTS_DEMO_ARCHIVE)
                .importFrom(new ZipFile(XTS_DEMO_DIR + XTS_DEMO_ARCHIVE)).as(EnterpriseArchive.class);
        return archive;
    }

    @Test
    public void testAtomicTransaction() throws Exception {
        testReservation(TX_TYPE_AT, "6", "1", "2", "3", true);
    }

    @Test
    public void testBusinessActivity() throws Exception {
        testReservation(TX_TYPE_BA, "8", "4", "2", "2", true);
    }

    protected void testReservation(String txType, String restaurantSeats, String theaterCircleSeats, String theaterStallsSeats, String theaterBalconySeats, boolean taxi) throws Exception {
        log.info("contextPath = " + contextPath);
        driver.open(new URL(contextPath + "/" + DEMO_APP_CONTEXT + "/"));

        waitModel.until(elementPresent.locator(TX_TYPE_FIELD));
        log.info("driver.getTitle() = " + driver.getTitle());

        driver.type(TX_TYPE_FIELD, txType);
        driver.type(RESTAURANT_FIELD, restaurantSeats);
        driver.type(THEATER_CIRCLE_FIELD, theaterCircleSeats);
        driver.type(THEATER_STALLS_FIELD, theaterStallsSeats);
        driver.type(THEATER_BALCONY_FIELD, theaterBalconySeats);
        driver.type(TAXI_FIELD, taxi ? "1" : "0");

        waitForHttp(driver).click(SUBMIT_BUTTON);

        String resultTableTitle = driver.getText(RESULT_TABLE_TITLE_XP);
        log.info("resultTableTitle = " + resultTableTitle);
        Assert.assertTrue("Page does not contain any results!", resultTableTitle.contains(RESULT_TITLE));

        String resultTableContent = driver.getText(RESULT_TABLE_CONTENT_XP);
        log.info("resultTableContent = " + resultTableContent);
        Assert.assertTrue("Transaction failed with: " + resultTableContent, resultTableContent.contains(TRANSACTION_FINISHED));
    }

}
