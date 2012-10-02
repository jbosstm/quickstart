import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJBImpl;
import org.junit.Test;

public class CallJTSCMTBean {

	@Test
	public void test() throws NamingException, RemoteException {
		final Hashtable jndiProperties = new Hashtable();
		jndiProperties.put(Context.URL_PKG_PREFIXES,
				"org.jboss.ejb.client.naming");
		jndiProperties.put("java.naming.factory.initial", "org.jboss.as.naming.InitialContextFactory");
		final Context context = new InitialContext(jndiProperties);
		final String appName = "";
		final String moduleName = "jts-quickstart";
		final String distinctName = "";
		final String beanName = CustomerManagerEJBImpl.class.getSimpleName();
		final String viewClassName = CustomerManagerEJB.class.getName();
		final CustomerManagerEJB statelessRemoteCalculator = (CustomerManagerEJB) context
				.lookup("ejb:" + appName + "/" + moduleName + "/"
						+ distinctName + "/" + beanName + "!" + viewClassName);
		statelessRemoteCalculator.createCustomer("foo");
	}
}
