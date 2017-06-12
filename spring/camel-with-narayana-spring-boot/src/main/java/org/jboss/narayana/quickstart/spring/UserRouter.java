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
