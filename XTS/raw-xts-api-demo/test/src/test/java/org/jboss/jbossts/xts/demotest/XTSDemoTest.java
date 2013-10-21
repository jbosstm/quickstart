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

import org.jboss.arquillian.container.test.api.Deployment;
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

import org.openqa.selenium.By;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.concurrent.TimeUnit;

import java.net.URL;
import java.util.zip.ZipFile;

/**
 * Basic tests for XTS demo application.
 *
 * @author istudens@redhat.com
 */
@RunWith(Arquillian.class)
public class XTSDemoTest {
    private static final Logger log = Logger.getLogger(XTSDemoTest.class);
    private static final int IMPLICIT_WAIT_S = 120;

    private static final String XTS_DEMO_DIR = "../ear/target/xts-demo-ear-5.0.0.Final-SNAPSHOT.ear";
    private static final String XTS_DEMO_ARCHIVE = "xts-demo.ear";
    private static final String DEMO_APP_CONTEXT = "xts-demo";

    private static final String RESULT_TITLE = "Transaction Result";
    private static final String TRANSACTION_FINISHED = "Transaction finished OK";

    private static final String TX_TYPE_AT = "AtomicTransaction";
    private static final String TX_TYPE_BA = "BusinessActivity";


    @Drone
    private HtmlUnitDriver driver;

    // Load context path to the test
    @ArquillianResource
    private URL contextPath;


    private static final By TX_TYPE_FIELD = By.name("txType");
    private static final By RESTAURANT_FIELD = By.name("restaurant");
    private static final By THEATER_CIRCLE_FIELD = By.name("theatrecirclecount");
    private static final By THEATER_STALLS_FIELD = By.name("theatrestallscount");
    private static final By THEATER_BALCONY_FIELD = By.name("theatrebalconycount");
    private static final By TAXI_FIELD = By.name("taxi");

    private static final By SUBMIT_BUTTON = By.name("submit");

    private static final By RESULT_TABLE_TITLE_XP = By.xpath("//div[@class='result_title']");
    private static final By RESULT_TABLE_CONTENT_XP = By.xpath("//div[@class='result']");


    @Deployment(name = XTS_DEMO_ARCHIVE, testable = false)
    public static Archive<?> createTestArchive() throws Exception {
        Archive<?> archive = ShrinkWrap.create(ZipImporter.class, XTS_DEMO_ARCHIVE)
                .importFrom(new ZipFile(XTS_DEMO_DIR)).as(EnterpriseArchive.class);
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
        driver.get(contextPath + "/" + DEMO_APP_CONTEXT + "/");

        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_S, TimeUnit.SECONDS);
        driver.findElement(TX_TYPE_FIELD);
        log.info("driver.getTitle() = " + driver.getTitle());

        
        driver.findElement(TX_TYPE_FIELD).sendKeys(txType);
        driver.findElement(RESTAURANT_FIELD).sendKeys(restaurantSeats);
        driver.findElement(THEATER_CIRCLE_FIELD).sendKeys(theaterCircleSeats);
        driver.findElement(THEATER_STALLS_FIELD).sendKeys(theaterStallsSeats);
        driver.findElement(THEATER_BALCONY_FIELD).sendKeys(theaterBalconySeats);
        driver.findElement(TAXI_FIELD).sendKeys(taxi ? "1" : "0");

        driver.findElement(SUBMIT_BUTTON).click();

        String resultTableTitle = driver.findElement(RESULT_TABLE_TITLE_XP).getText();
        log.info("resultTableTitle = " + resultTableTitle);
        Assert.assertTrue("Page does not contain any results!", resultTableTitle.contains(RESULT_TITLE));

        String resultTableContent = driver.findElement(RESULT_TABLE_CONTENT_XP).getText();
        log.info("resultTableContent = " + resultTableContent);
        Assert.assertTrue("Transaction failed with: " + resultTableContent, resultTableContent.contains(TRANSACTION_FINISHED));
    }

}
