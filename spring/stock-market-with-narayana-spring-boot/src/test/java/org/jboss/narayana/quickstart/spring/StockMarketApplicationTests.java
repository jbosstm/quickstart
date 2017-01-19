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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Integration test for the StockMarketApplication.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class StockMarketApplicationTests {

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
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Before
    public void before() {
        userRepository.deleteAll();
        shareRepository.deleteAll();
        portfolioEntryRepository.deleteAll();
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testBuyAndSell() throws Exception {
        createUser(USERNAME, BUDGET);
        createShare(SYMBOL, AMOUNT, PRICE);

        buy(USERNAME, SYMBOL, 1);
        assertUser(USERNAME, BUDGET - PRICE, Collections.singletonMap(SYMBOL, 1));
        assertShare(SYMBOL, AMOUNT - 1, PRICE);

        sell(USERNAME, SYMBOL, 1);
        assertUser(USERNAME, BUDGET, Collections.singletonMap(SYMBOL, 0));
        assertShare(SYMBOL, AMOUNT, PRICE);
    }

    private void createUser(String username, int budget) throws Exception {
        ObjectNode user = objectMapper
                .createObjectNode()
                .put("username", username)
                .put("budget", budget);

        mockMvc
                .perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(user.toString()))
                .andExpect(status().isCreated());
    }

    private void createShare(String symbol, int amount, int price) throws Exception {
        ObjectNode share = objectMapper
                .createObjectNode()
                .put("symbol", symbol)
                .put("amount", amount)
                .put("price", price);

        mockMvc
                .perform(post("/shares")
                        .contentType(APPLICATION_JSON)
                        .content(share.toString()))
                .andExpect(status().isCreated());
    }

    private void buy(String username, String symbol, int amount) throws Exception {
        mockMvc
                .perform(put(String.format("/portfolio/%s/%s?amount=%d", username, symbol, amount)))
                .andExpect(status().isOk());
    }

    private void sell(String username, String symbol, int amount) throws Exception {
        mockMvc
                .perform(delete(String.format("/portfolio/%s/%s?amount=%d", username, symbol, amount)))
                .andExpect(status().isOk());
    }

    private void assertUser(String username, int budget, Map<String, Integer> portfolioEntries) throws Exception {
        ResultActions actions = mockMvc
                .perform(get(String.format("/users/%s", username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budget", is(budget)))
                .andExpect(jsonPath("$.portfolio", hasSize(portfolioEntries.size())));

        int i = 0;
        for (Map.Entry<String, Integer> entry : portfolioEntries.entrySet()) {
            actions.andExpect(jsonPath("$.portfolio[" + i + "].amount", is(entry.getValue())));
            actions.andExpect(jsonPath("$.portfolio[" + i + "]._links.share.href", endsWith(entry.getKey())));
            i++;
        }
    }

    private void assertShare(String symbol, int amount, int price) throws Exception {
        mockMvc
                .perform(get(String.format("/shares/%s", symbol)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(price)))
                .andExpect(jsonPath("$.amount", is(amount)));
    }

}
