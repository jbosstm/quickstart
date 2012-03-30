package org.jboss.narayana.blacktie.jatmibroker;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.server.BlackTieServer;

public class BlackTieServerLauncher {

    /**
     * @param args
     * @throws ConnectionException
     * @throws ConfigurationException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ConfigurationException, ConnectionException, InterruptedException {
        BlackTieServer server = new BlackTieServer("javaser");
        server.tpadvertise("JAVASERV", BarService.class.getName());
        server.block();
    }

}
