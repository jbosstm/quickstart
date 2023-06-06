package org.jboss.narayana.quickstart.spring;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Component
public class UserBean {
    public User generateUser(String name) {
        User user = new User();
        user.setName(name);
        return user;
    }
}