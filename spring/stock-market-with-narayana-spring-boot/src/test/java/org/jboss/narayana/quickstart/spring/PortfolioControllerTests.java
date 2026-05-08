package org.jboss.narayana.quickstart.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Unit tests for the PortfolioController.
 */
@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    public void before() {
        initUser();
        initShare();
    }

    @Test
    public void testBuyWithNotExistingUser() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.buy(USERNAME + "_", SYMBOL, 1));
    }

    @Test
    public void testBuyNotExistingShare() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.buy(USERNAME, SYMBOL + "_", 1));
    }

    @Test
    public void testBuyOverBudget() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.buy(USERNAME, SYMBOL, AMOUNT));
    }

    @Test
    public void testBuyOverAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.buy(USERNAME, SYMBOL, AMOUNT + 1));
    }

    @Test
    public void testSellWithNotExistingUser() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.sell(USERNAME + "_", SYMBOL, 1));
    }

    @Test
    public void testSellNotExistingShare() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.sell(USERNAME, SYMBOL + "_", 1));
    }

    @Test
    public void testSellOverAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                portfolioController.sell(USERNAME, SYMBOL, 1));
    }

    @Test
    public void testBuyAndSell() {
        portfolioController.buy(USERNAME, SYMBOL, 1);
        User user = userRepository.findById(USERNAME).orElseThrow();
        assertEquals(BUDGET - PRICE, user.getBudget());
        Share share = shareRepository.findById(SYMBOL).orElseThrow();
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
