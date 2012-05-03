package org.jboss.as.quickstarts.wsat.jtabridge.fromjta;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import org.jboss.as.quickstarts.wsat.jtabridge.jaxws.RestaurantServiceAT;
import org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author paul.robinson@redhat.com, 2012-05-03
 */
public class ATBridgeClient {

    public static RestaurantServiceAT newInstance() throws Exception {
        URL wsdlLocation = new URL("http://localhost:8080/test/RestaurantServiceATService/RestaurantServiceAT?wsdl");
        QName serviceName = new QName("http://www.jboss.com/jbossas/quickstarts/wsat/simple/Restaurant", "RestaurantServiceATService");
        QName portName = new QName("http://www.jboss.com/jbossas/quickstarts/wsat/simple/Restaurant", "RestaurantServiceAT");

        Service service = Service.create(wsdlLocation, serviceName);
        RestaurantServiceAT client = service.getPort(portName, RestaurantServiceAT.class);

        /*
         * Add client handler chain so that XTS can add the transaction context to the SOAP messages.
         *
         * This will be automatically added by the TXFramework in the future.
         */
        BindingProvider bindingProvider = (BindingProvider) client;
        List<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new JaxWSTxOutboundBridgeHandler());
        handlers.add(new JaxWSHeaderContextProcessor());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return client;
    }
}

