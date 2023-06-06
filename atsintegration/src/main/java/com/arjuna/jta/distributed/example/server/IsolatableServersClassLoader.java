package com.arjuna.jta.distributed.example.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * This classloader will reload copies of classes (except a package that is
 * configured for ignoring - the interfaces that the root example requires
 * basically).
 */
public class IsolatableServersClassLoader extends ClassLoader {
	private Map<String, Class<?>> clazzMap = new HashMap<String, Class<?>>();
	private URLClassLoader urlClassLoader;
	private String ignoredPackage;
	private String includedPackage;
	private String otherIgnoredPackage;

	/**
	 * Create the classloader.
	 * 
	 * @param ignoredPackage
	 *            This package will be ignored by this classloader and delegated
	 *            to its parent so that the example testcase can access required
	 *            interfaces of its test.
	 * @param parent
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws MalformedURLException
	 */

	public IsolatableServersClassLoader(String includedPackage, String ignoredPackage, ClassLoader parent) throws SecurityException,
			MalformedURLException {
		super(parent);
		this.includedPackage = includedPackage;
		this.otherIgnoredPackage = ignoredPackage;
		this.ignoredPackage = IsolatableServersClassLoader.class.getPackage().getName();
		String property = System.getProperty("java.class.path");
		String[] split = property.split(System.getProperty("path.separator"));
		URL[] urls = new URL[split.length];
		for (int i = 0; i < urls.length; i++) {
			String url = split[i];
			if (url.endsWith(".jar")) {
				urls[i] = new URL("jar:file:" + url + "!/");
			} else {
				urls[i] = new URL("file:" + url + "/");
			}
		}
		urlClassLoader = URLClassLoader.newInstance(urls);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (clazzMap.containsKey(name)) {
			return clazzMap.get(name);
		}
		return super.findClass(name);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!name.matches(ignoredPackage + ".[A-Za-z0-9]*") && otherIgnoredPackage != null && name.startsWith(otherIgnoredPackage)) {
			throw new ClassNotFoundException(name);
		}
		Class<?> clazz = null;
		if (clazzMap.containsKey(name)) {
			clazz = clazzMap.get(name);
		}

		if (clazz != null) {
			System.err.println("Already loaded: " + name);
		} else {
			if (!name.startsWith("com.arjuna") || name.matches(ignoredPackage + ".[A-Za-z0-9]*")
					|| (includedPackage != null && !name.startsWith(includedPackage))) {
				clazz = getParent().loadClass(name);
			} else {
				clazz = urlClassLoader.loadClass(name);
				clazzMap.put(name, clazz);
			}

		}
		return clazz;
	}
}