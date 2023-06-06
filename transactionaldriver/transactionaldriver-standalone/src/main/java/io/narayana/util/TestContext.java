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