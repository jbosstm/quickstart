package org.jboss.narayana.quickstarts.wsat.jtabridge.second;

import org.jboss.narayana.quickstarts.wsat.jtabridge.second.jaxws.SecondServiceAT;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

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

        return client;
    }
}

