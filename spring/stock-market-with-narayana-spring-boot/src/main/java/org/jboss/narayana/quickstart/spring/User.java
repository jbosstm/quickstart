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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Entity
public class User {

    /**
     * Unique username.
     */
    @Id
    private String username;

    /**
     * Current budget to buy shares.
     */
    private int budget;

    /**
     * Shares owned by this user.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<PortfolioEntry> portfolio;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public Set<PortfolioEntry> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Set<PortfolioEntry> portfolio) {
        this.portfolio = portfolio;
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', budget=%d, portfolio=%s}", username, budget, portfolio);
    }

}
