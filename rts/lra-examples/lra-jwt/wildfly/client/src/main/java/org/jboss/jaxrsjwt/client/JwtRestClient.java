package org.jboss.jaxrsjwt.client;

import org.jboss.jaxrsjwt.auth.JwtManager;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

public class JwtRestClient {
    private static final String AUTHZ_HEADER = "Authorization";
    private static final String REST_TARGET_URL = "http://localhost:8080/lra-coordinator/lra-coordinator";
    private static final String ADMIN_ROLE = "admin";
    private static final String PARTICIPANT_ROLE = "participant";

    private String role;

    public JwtRestClient() {
        role = null;
    }

    public JwtRestClient(String role) {
        this.role = role;
    }

    public static void main(String[] args) throws Exception{
        //participant role
        JwtRestClient participantClient = new JwtRestClient(PARTICIPANT_ROLE);
        participantClient.test(true);
        //admin role
        JwtRestClient adminClient = new JwtRestClient(ADMIN_ROLE);
        adminClient.test(true);
        //test without token
        JwtRestClient noToken = new JwtRestClient();
        noToken.test(false);

    }

    public void test(boolean obtainToken) throws Exception{
        System.out.println("------------------------------");
        System.out.printf("Testing %s ",(role != null ? role : "without token"));
        System.out.println("------------------------------");
        String authzHeaderValue = null;
        if (obtainToken) {
            System.out.println("Obtaining JWT...");
            JwtManager jwtManager = new JwtManager();
            String jwt = jwtManager.createJwt(role, role);
            authzHeaderValue = "Bearer " + jwt;
        }
        System.out.printf("%s jwt : %s",role, authzHeaderValue);
        getUsingJwt("/start", role, authzHeaderValue);
        getUsingJwt("/", role, authzHeaderValue);

    }

    private void getUsingJwt(String path,String role, String authzHeaderValue) {
        System.out.println("Accessing " + path + "...");
        Response response = null;
        response = ClientBuilder.newClient().target(REST_TARGET_URL).path(path).request().header(AUTHZ_HEADER, authzHeaderValue).get();
        System.out.println("=========================================================");
        System.out.printf("path called : %s, role : %s , ", path, role);
        System.out.println("Status: " + response.getStatus()+"\n" + (response.getStatus() == 200 ? response.readEntity(String.class)+"\n" : ""));
        System.out.println("=========================================================");
        response.close();
    }
}