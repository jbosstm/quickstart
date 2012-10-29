package org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;
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
public class FirstClient {

    public static FirstServiceAT newInstance() throws Exception {
        URL wsdlLocation = new URL("http://localhost:8080/test/FirstServiceATService/FirstServiceAT?wsdl");
        QName serviceName = new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/first", "FirstServiceATService");
        QName portName = new QName("http://www.jboss.org/narayana/quickstarts/wsat/simple/first", "FirstServiceAT");

        Service service = Service.create(wsdlLocation, serviceName);
        FirstServiceAT client = service.getPort(portName, FirstServiceAT.class);

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

