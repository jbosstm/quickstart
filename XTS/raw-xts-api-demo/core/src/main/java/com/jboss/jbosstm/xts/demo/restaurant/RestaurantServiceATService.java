/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 */

package com.jboss.jbosstm.xts.demo.restaurant;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "RestaurantServiceATService", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Restaurant", wsdlLocation = "/WEB-INF/wsdl/RestaurantServiceAT.wsdl")
public class RestaurantServiceATService
    extends Service
{

    private final static URL RESTAURANTSERVICEATSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(com.jboss.jbosstm.xts.demo.restaurant.RestaurantServiceATService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.jboss.jbosstm.xts.demo.restaurant.RestaurantServiceATService.class.getResource(".");
            url = new URL(baseUrl, "/WEB-INF/wsdl/RestaurantServiceAT.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: '/WEB-INF/wsdl/RestaurantServiceAT.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        RESTAURANTSERVICEATSERVICE_WSDL_LOCATION = url;
    }

    public RestaurantServiceATService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RestaurantServiceATService() {
        super(RESTAURANTSERVICEATSERVICE_WSDL_LOCATION, new QName("http://www.jboss.com/jbosstm/xts/demo/Restaurant", "RestaurantServiceATService"));
    }

    /**
     * 
     * @return
     *     returns IRestaurantServiceAT
     */
    @WebEndpoint(name = "RestaurantServiceAT")
    public IRestaurantServiceAT getRestaurantServiceAT() {
        return super.getPort(new QName("http://www.jboss.com/jbosstm/xts/demo/Restaurant", "RestaurantServiceAT"), IRestaurantServiceAT.class);
    }

}
