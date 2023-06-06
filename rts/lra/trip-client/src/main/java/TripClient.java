import com.fasterxml.jackson.databind.ObjectMapper;
import model.Booking;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Scanner;

public class TripClient {
    static ObjectMapper objectMapper = new ObjectMapper();
    private static String TRIP_SERVICE_BASE_URL;

    public static void main(String[] args) throws Exception {
        String serviceHost = System.getProperty("service.http.host", "localhost");
        int servicePort = Integer.getInteger("service.http.port", 8084);

        TRIP_SERVICE_BASE_URL = "http://" + serviceHost + ":" + servicePort;

        TripClient tripClient = new TripClient();

        Booking booking = tripClient.bookTrip("TheGrand", "BA123", "RH456");

        if (booking == null)
            return;

        System.out.printf("%nBooking Info:%n\t%s%n", booking);

        Arrays.stream(booking.getDetails()).forEach(b -> System.out.printf("\tAssociated Booking: %s%n", b));

        boolean cancel = false;
        if (args.length == 0) {
            System.out.println("Cancel (y) or Confirm (n) ?");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            cancel = reader.readLine().equals("y");
            reader.close();
        } else if (args[0].equals("cancel")) {
            cancel = true;
        }

        System.out.println("Will cancel?: " + cancel);

        Booking response = (cancel)
                ? tripClient.cancelTrip(booking)
                : tripClient.confirmTrip(booking);

        System.out.printf("%nBooking Info:%n\t%s%n", response);
        Arrays.stream(response.getDetails()).forEach(b -> System.out.printf("\tAssociated Booking: %s%n", b));
    }

    private Booking bookTrip(String hotelName, String flightNumber, String altFlightNumber) throws Exception {
        StringBuilder tripRequest =
                new StringBuilder(TRIP_SERVICE_BASE_URL)
                        .append("/?")
                        .append("hotelName").append('=').append(hotelName).append('&')
                        .append("flightNumber1").append('=').append(flightNumber).append('&')
                        .append("flightNumber2").append('=').append(altFlightNumber);

        URL url = new URL(tripRequest.toString());
        String json = updateResource(url, "POST", "");

        return objectMapper.readValue(json, Booking.class);
    }

    private Booking cancelTrip(Booking booking) throws Exception {
        StringBuilder tripRequest =
                new StringBuilder(TRIP_SERVICE_BASE_URL)
                        .append("/")
                        .append(URLEncoder.encode(booking.getId().toString(), "UTF-8"));

        URL url = new URL(tripRequest.toString());
        String json = updateResource(url, "DELETE", "");

        return objectMapper.readValue(json, Booking.class);
    }

    private Booking confirmTrip(Booking booking) throws Exception {
        StringBuilder tripRequest =
                new StringBuilder(TRIP_SERVICE_BASE_URL)
                        .append("/")
                        .append(URLEncoder.encode(booking.getId().toString(), "UTF-8"));

        URL url = new URL(tripRequest.toString());
        String json = updateResource(url, "PUT", "");

        return objectMapper.readValue(json, Booking.class);
    }

    private String updateResource(URL resource, String method, String jsonBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) resource.openConnection();

        try (AutoCloseable ac = connection::disconnect) {

            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");

            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
                dos.writeBytes(jsonBody);
            }

            int responseCode = connection.getResponseCode();

            try (InputStream ins = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                Scanner responseScanner = new java.util.Scanner(ins).useDelimiter("\\A");
                String res = responseScanner.hasNext() ? responseScanner.next() : null;

                if (res != null && responseCode >= 400) {
                    System.err.printf("Error to contact (%s) '%s', error:%n%s%n",
                        method, resource, res);

                    return null;
                }

                return res;
            }
        }
    }
}