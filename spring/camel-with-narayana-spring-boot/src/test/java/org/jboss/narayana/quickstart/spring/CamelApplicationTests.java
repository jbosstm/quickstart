package org.jboss.narayana.quickstart.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class CamelApplicationTests {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private UserRepository userRepository;

	@Test
	public void testCommit() throws Exception {
        jmsTemplate.convertAndSend("foo", "test");
        Thread.sleep(1 * 1000);
        assertEquals(userRepository.count(), 1);
    }

    @Test
    public void testRollback() throws Exception {
        jmsTemplate.convertAndSend("foo", "bad");
        Thread.sleep(1 * 1000);
        assertEquals(userRepository.count(), 0);
    }
}