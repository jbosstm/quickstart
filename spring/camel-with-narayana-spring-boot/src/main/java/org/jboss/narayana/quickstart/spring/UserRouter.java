package org.jboss.narayana.quickstart.spring;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
@Component
public class UserRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("jms:queue:foo?transacted=true")
                .transacted()
                .to("jms:queue:users")
                .bean(UserBean.class)
                .to("jpa:org.jboss.narayana.quickstart.spring.User")
                .log("Inserted new User ${body.id} with Name ${body.name}")
                .choice()
                    .when(simple("${body.name} contains 'bad'"))
                        .throwException(new IllegalArgumentException("bad name"))
                    .when(simple("${body.name} contains 'halt'"))
                        .bean(HaltBean.class)
                    .otherwise()
                        .to("mock:result")
                .endChoice();

        from("jms:queue:users")
                .bean(UserBean.class)
                .log("Get user with name ${body.name}");

        from("jms:queue:DLQ")
                .bean(UserBean.class)
                .log("Can not process user with name ${body.name}");
    }
}