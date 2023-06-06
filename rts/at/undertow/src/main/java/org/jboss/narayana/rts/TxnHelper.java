package org.jboss.narayana.rts;

import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatusMediaType;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.Set;

public class TxnHelper {
    public static Link getLink(Set<Link> links, String relation) {
        for (Link link : links)
            if (link.getRel().equals(relation))
                return link;

        return null;
    }

    public static Set<Link> beginTxn(Client client, String txurl) {

        try (Response response = client.target(txurl).request().post(Entity.form(new Form()))) {
            Set<Link> links = response.getLinks();

            if (response.getStatus() != HttpURLConnection.HTTP_CREATED)
                throw new RuntimeException("beginTxn returned " + response.getStatus());

            return links;
        }
    }

    public static int endTxn(Client client, Set<Link> links) {

        try (Response response = client.target(Objects.requireNonNull(getLink(links, TxLinkNames.TERMINATOR)).getUri())
                .request().put(Entity.entity(TxStatusMediaType.TX_COMMITTED, TxMediaType.TX_STATUS_MEDIA_TYPE))) {

            int sc = response.getStatus();
            if (sc != HttpURLConnection.HTTP_OK)
                throw new RuntimeException("endTxn returned " + sc);

            return sc;
        }
    }
}