package org.jboss.narayana.quickstart.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class CamelApplicationTests {
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testCommit() throws Exception {
		long initialCount = userRepository.count();
		jmsTemplate.convertAndSend("foo", "test");
		Thread.sleep(1 * 1000);
		assertEquals(initialCount + 1, userRepository.count());
	}

	@Test
	public void testRollback() throws Exception {
		long initialCount = userRepository.count();
		jmsTemplate.convertAndSend("foo", "bad");
		Thread.sleep(1 * 1000);
		assertEquals(initialCount, userRepository.count());
	}
}
