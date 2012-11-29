package org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler;
import org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws.SecondServiceAT;

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
public class SecondClient {

    public static SecondServiceAT newInstance() throws Exception {
        URL wsdlLocation = new URL("http://localhost:8080/test/SecondServiceATService/SecondServiceAT?wsdl");
        QName serviceName = new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/second", "SecondServiceATService");
        QName portName = new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/second", "SecondServiceAT");

        Service service = Service.create(wsdlLocation, serviceName);
        SecondServiceAT client = service.getPort(portName, SecondServiceAT.class);

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

