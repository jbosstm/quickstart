/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2010, Red Hat, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * @author JBoss Inc.
 */
/*
 * SecondManager.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: SecondManager.java,v 1.3 2004/04/21 13:09:18 jhalliday Exp $
 *
 */
package org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws;

import org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws.SecondServiceAT;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.6 in JDK 6 Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "SecondServiceATService", targetNamespace = "http://www.jboss.org/narayana/quickstarts/wsat/simple/second")
public class SecondServiceATService extends Service {

    private final static URL SECONDSERVICEATSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(SecondServiceATService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = SecondServiceATService.class.getResource(".");
            url = new URL(baseUrl, "SecondServiceAT.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'SecondServiceAT.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        SECONDSERVICEATSERVICE_WSDL_LOCATION = url;
    }

    public SecondServiceATService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SecondServiceATService() {
        super(SECONDSERVICEATSERVICE_WSDL_LOCATION, new QName(
                "http://www.jboss.org/narayana/quickstarts/wsat/simple/Second", "SecondServiceATService"));
    }

    /**
     * 
     * @return returns SecondServiceAT
     */
    @WebEndpoint(name = "SecondServiceAT")
    public SecondServiceAT getSecondServiceAT() {
        return super.getPort(
                new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/Second", "SecondServiceAT"),
                SecondServiceAT.class);
    }

    /**
     * 
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy. Supported features not in the
     *        <code>features</code> parameter will have their default values.
     * @return returns SecondServiceAT
     */
    @WebEndpoint(name = "SecondServiceAT")
    public SecondServiceAT getSecondServiceAT(WebServiceFeature... features) {
        return super.getPort(
                new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/Second", "SecondServiceAT"),
                SecondServiceAT.class, features);
    }

}
