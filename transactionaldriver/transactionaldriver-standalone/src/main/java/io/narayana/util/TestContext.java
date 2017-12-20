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

package io.narayana.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Simple context implementation used as a stub solution
 * for the quickstart purposes.
 */
public class TestContext implements Context {
    private final Map<String, Object> table = new HashMap<>();

    @Override
    public Object lookup(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return table.get(name);
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        table.put(name, obj);

    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        table.put(name, obj);

    }

    @Override
    public void unbind(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void unbind(String name) throws NamingException {
        table.remove(name);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        Object value = table.get(oldName);
        table.remove(oldName);
        table.put(newName, value);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }

    @Override
    public void close() throws NamingException {
        // nothing
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException("not supported in this testing context");
    }
}
