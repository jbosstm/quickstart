package org.jboss.narayana.quickstart.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Controller
public class UserController {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/user")
    @ResponseBody
    @ResponseStatus( HttpStatus.OK )
    public void create(@ModelAttribute User user) {
        jmsTemplate.convertAndSend("foo", user.getName());
    }

    @GetMapping("/users")
    @ResponseBody
    @ResponseStatus( HttpStatus.OK )
    public String list() {
        StringBuffer resp = new StringBuffer();
        for(User user :userRepository.findAll()) {
            resp.append(user).append("\n");
        }
        return resp.toString();
    }
}