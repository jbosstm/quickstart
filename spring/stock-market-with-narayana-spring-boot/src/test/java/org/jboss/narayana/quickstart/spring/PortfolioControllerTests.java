/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.quickstart.spring;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit tests for the PortfolioController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PortfolioControllerTests {

    private static final String USERNAME = "test-user";

    private static final String SYMBOL = "test-share";

    private static final int BUDGET = 1000;

    private static final int AMOUNT = 100;

    private static final int PRICE = 100;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private PortfolioEntryRepository portfolioEntryRepository;

    @Autowired
    private PortfolioController portfolioController;

    @Before
    public void before() {
        initUser();
        initShare();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyWithNotExistingUser() {
        portfolioController.buy(USERNAME + "_", SYMBOL, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyNotExistingShare() {
        portfolioController.buy(USERNAME, SYMBOL + "_", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyOverBudget() {
        portfolioController.buy(USERNAME, SYMBOL, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyOverAmount() {
        portfolioController.buy(USERNAME, SYMBOL, AMOUNT + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellWithNotExistingUser() {
        portfolioController.sell(USERNAME + "_", SYMBOL, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellNotExistingShare() {
        portfolioController.sell(USERNAME, SYMBOL + "_", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellOverAmount() {
        portfolioController.sell(USERNAME, SYMBOL, 1);
    }

    @Test
    public void testBuyAndSell() {
        portfolioController.buy(USERNAME, SYMBOL, 1);
        User user = userRepository.getOne(USERNAME);
        assertEquals(BUDGET - PRICE, user.getBudget());
        Share share = shareRepository.getOne(SYMBOL);
        assertEquals(AMOUNT - 1, share.getAmount());
        PortfolioEntry portfolioEntry = portfolioEntryRepository.findByUserAndShare(user, share);
        assertEquals(1, portfolioEntry.getAmount());
    }

    private void initUser() {
        userRepository.deleteAll();
        User user = new User();
        user.setUsername(USERNAME);
        user.setBudget(BUDGET);
        userRepository.save(user);
    }

    private void initShare() {
        shareRepository.deleteAll();
        Share share = new Share();
        share.setSymbol(SYMBOL);
        share.setPrice(PRICE);
        share.setAmount(AMOUNT);
        shareRepository.save(share);
    }

}
