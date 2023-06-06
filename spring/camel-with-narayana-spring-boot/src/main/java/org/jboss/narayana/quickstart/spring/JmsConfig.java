package org.jboss.narayana.quickstart.spring;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Configuration
@ConfigurationProperties(prefix = "spring.artemis.embedded")
public class JmsConfig implements ArtemisConfigurationCustomizer {
    public void customize(org.apache.activemq.artemis.core.config.Configuration configuration) {
        AddressSettings settings = new AddressSettings();
        settings.setMaxDeliveryAttempts(1);
        settings.setExpiryAddress(new SimpleString("jms.queue.DLQ"));
        settings.setDeadLetterAddress(new SimpleString("jms.queue.DLQ"));
        configuration.addAddressesSetting("#", settings);
    }
}