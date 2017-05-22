/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package io.narayana;


import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class StringDao {

    public static EntityManager entityManager;

    /**
     * Get all strings from the database. This methods closes database connection itself, thus transaction is not needed.
     *
     * @return
     * @throws SQLException
     */
    public List<String> getAll() throws SQLException {
        List<StringEntity> entities = entityManager
                .createQuery("SELECT string FROM strings string", StringEntity.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
        List<String> strings = new ArrayList<String>();

        for (StringEntity entity : entities) {
            strings.add(entity.getValue());
        }
        return strings;
    }

    /**
     * Save string to the database. This method must be called inside a transaction, because connection is left open to be
     * closed by transaction manager.
     *
     * @param string
     * @throws SQLException
     */
    public void save(String string) throws SQLException {
        entityManager.persist(new StringEntity(string));
    }

}
