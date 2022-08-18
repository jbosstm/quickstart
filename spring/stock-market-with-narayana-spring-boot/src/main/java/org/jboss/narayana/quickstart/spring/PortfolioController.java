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

import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

/**
 * Portfolio controller exposing REST endpoints for buying and selling shares.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RestController
public class PortfolioController {

    private final UserRepository userRepository;

    private final ShareRepository shareRepository;

    private final PortfolioEntryRepository portfolioEntryRepository;

    private final JmsTemplate jmsTemplate;

    public PortfolioController(UserRepository userRepository, ShareRepository shareRepository,
            PortfolioEntryRepository portfolioEntryRepository, JmsTemplate jmsTemplate) {
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.portfolioEntryRepository = portfolioEntryRepository;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * An endpoint to buy shares.
     *
     * It can be invoked with a PUT request on /portfolio/{username}/{symbol}?amount={amount} endpoint.
     *
     * This method is transactional and all its work will be aborted if the following conditions are not met:
     * 1. User must exist.
     * 2. Stock must exist.
     * 3. User must have enough budget.
     * 4. There must be enough shares available to be bought.
     *
     * During the execution, status messages will be sent to the "updates" queue.
     *
     * @param username A unique name of a user who's buying the shares.
     * @param symbol A unique identifier of a share to be bought.
     * @param amount A number of shares to be bought.
     * @throws IllegalArgumentException if a user or a share doesn't exist, or if an amount or a budget is not sufficient.
     */
    @PutMapping("/portfolio/{username}/{symbol}")
    @Transactional
    public void buy(@PathVariable String username, @PathVariable String symbol, @RequestParam("amount") int amount) {
        User user = getUser(username);
        Share share = getShare(symbol);
        PortfolioEntry portfolioEntry = getOrCreatePortfolioEntry(user, share); // Get user's portfolio entry or create a new one

        updateBudget(user, -1 * amount * share.getPrice()); // Decrease user's budget
        updateSharesAmount(share, -1 * amount); // Decrease shares amount available for sale
        updatePortfolioEntry(portfolioEntry, amount); // Increase shares amount owned by the user
        sendUpdate(String.format("'%s' just bought %d shares of '%s' for the amount of %d", username, amount, symbol,
                amount * share.getPrice()));
    }

    /**
     * An endpoint to sell shares.
     *
     * It can be invoked with a DELETE request on /portfolio/{username}/{symbol}?amount={amount} endpoint.
     *
     * This method is transactional and all its work will be aborted if the following conditions are not met:
     * 1. User must exist.
     * 2. Stock must exist.
     * 3. User must have enough shares.
     *
     * During the execution, status messages will be sent to the "updates" queue.
     *
     * @param username A unique name of a user who's selling the shares.
     * @param symbol A unique identifier of a share to be sold.
     * @param amount A number of shares to be sold.
     * @throws IllegalArgumentException if a user or a share doesn't exist, or if user doesn't own enough shares.
     */
    @DeleteMapping("/portfolio/{username}/{symbol}")
    @Transactional
    public void sell(@PathVariable String username, @PathVariable String symbol, @RequestParam("amount") int amount) {
        User user = getUser(username);
        Share share = getShare(symbol);
        PortfolioEntry portfolioEntry = getPortfolioEntry(user, share);

        updateBudget(user, amount * share.getPrice()); // Increase user's budget
        updateSharesAmount(share, amount); // Increase shares amount available for sale
        updatePortfolioEntry(portfolioEntry, -1 * amount); // Decrease shares amount owned by the user
        sendUpdate(String.format("'%s' just sold %d shares of '%s' for the amount of %d", username, amount, symbol,
                amount * share.getPrice()));
    }

    /**
     * Update user's budget with a positive or a negative change.
     *
     * @param user A user who's budget has to be updated.
     * @param change A positive or a negative amount to be added to the budget.
     * @throws IllegalArgumentException if a change causes user's budget to become negative.
     */
    private void updateBudget(User user, int change) {
        if (user.getBudget() + change < 0) {
            throw new IllegalArgumentException(String.format("Budget cannot be negative (%d)", user.getBudget() + change));
        }

        user.setBudget(user.getBudget() + change);
        userRepository.save(user);
        sendUpdate(String.format("Updated '%s' budget to %d", user.getUsername(), user.getBudget()));
    }

    /**
     * Update an amount of shares available in the market with a positive or a negative change.
     *
     * @param share A share which amount has to be updated.
     * @param change A positive or a negative amount to be added to the share's amount.
     * @throws IllegalArgumentException if a change causes share's amount to become negative.
     */
    private void updateSharesAmount(Share share, int change) {
        if (share.getAmount() + change < 0) {
            throw new IllegalArgumentException(
                    String.format("Shares amount cannot be negative (%d)", share.getAmount() + change));
        }

        share.setAmount(share.getAmount() + change);
        shareRepository.save(share);
        sendUpdate(String.format("Updated '%s' amount to %d", share.getSymbol(), share.getAmount()));
    }

    /**
     * Update user's portfolio entry with a positive or a negative change.
     *
     * @param portfolioEntry A portfolio entry to be updated.
     * @param change A positive or negative amount to be added to the share's amount in the portfolio.
     * @throws IllegalArgumentException if a change causes share's amount to become negative.
     */
    private void updatePortfolioEntry(PortfolioEntry portfolioEntry, int change) {
        if (portfolioEntry.getAmount() + change < 0) {
            throw new IllegalArgumentException(
                    String.format("Shares amount cannot be negative (%d)", portfolioEntry.getAmount() + change));
        }

        portfolioEntry.setAmount(portfolioEntry.getAmount() + change);
        portfolioEntryRepository.save(portfolioEntry);
    }

    /**
     * Get a user by his username.
     *
     * @param username A username of a user.
     * @return A user found by his username.
     * @throws IllegalArgumentException if a user doesn't exist.
     */
    private User getUser(String username) {
        User user = userRepository.getOne(username);
        if (user == null) {
            throw new IllegalArgumentException(String.format("User '%s' not found", username));
        }

        return user;
    }

    /**
     * Get a share by its symbol.
     *
     * @param symbol A unique symbol of a share.
     * @return A share found by its symbol.
     * @throws IllegalArgumentException if the share doesn't exist.
     */
    private Share getShare(String symbol) {
        Share share = shareRepository.getOne(symbol);
        if (share == null) {
            throw new IllegalArgumentException(String.format("Share '%s' not found", symbol));
        }

        return share;
    }

    /**
     * Get a portfolio entry by a user and a share.
     *
     * @param user A user whose portfolio needs to be found.
     * @param share A share which portfolio needs to be found.
     * @return A portfolio entry found by its user and share.
     * @throws IllegalArgumentException if the portfolio doesn't exist.
     */
    private PortfolioEntry getPortfolioEntry(User user, Share share) {
        PortfolioEntry portfolioEntry = portfolioEntryRepository.findByUserAndShare(user, share);
        if (portfolioEntry == null) {
            throw new IllegalArgumentException("Portfolio entry not found");
        }

        return portfolioEntry;
    }

    /**
     * Get a portfolio entry by a user and a share or create one if it doesn't exist.
     *
     * @param user A user whose portfolio needs to be found.
     * @param share A share which portfolio needs to be found.
     * @return A portfolio entry found by its user and share or a new portfolio entry.
     */
    private PortfolioEntry getOrCreatePortfolioEntry(User user, Share share) {
        PortfolioEntry portfolioEntry = portfolioEntryRepository.findByUserAndShare(user, share);

        if (portfolioEntry == null) {
            portfolioEntry = new PortfolioEntry();
            portfolioEntry.setUser(user);
            portfolioEntry.setShare(share);
        }

        return portfolioEntry;
    }

    /**
     * Send a message to the "updates" queue.
     *
     * @param message A message to be sent.
     */
    private void sendUpdate(String message) {
        jmsTemplate.convertAndSend("updates", message);
    }

}
