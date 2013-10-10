package org.jboss.narayana.quickstarts.wsat.jtabridge.fromjta;

import org.jboss.narayana.quickstarts.wsat.jtabridge.first.jaxws.FirstServiceAT;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

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

        return client;
    }
}

