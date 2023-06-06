package org.jboss.narayana.quickstart.spring;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Component
public class HaltBean {
    public void halt(String name) {
        if (!System.getProperty("recover", "false").equals("true")) {
            Runtime.getRuntime().halt(0);
        }
    }
}