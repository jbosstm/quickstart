package io.narayana.sra.demo.constant;

public class ServiceConstant {
    public static final String HOTEL_PATH = "/hotel";
    public static final String HOTEL_NAME_PARAM = "hotelName";
    public static final String HOTEL_BEDS_PARAM = "beds";

    public static final String FLIGHT_PATH = "/flight";
    public static final String FLIGHT_NUMBER_PARAM = "flightNumber";
    public static final String ALT_FLIGHT_NUMBER_PARAM = "altFlightNumber";
    public static final String FLIGHT_SEATS_PARAM = "flightSeats";

    public static final String TRIP_PATH = "/trip";
    public static final String SERVICE_PORT_PROPERTY = "quarkus.http.port";

    public static final String HOTEL_SERVICE_PORT_PROPERTY = "hotel.quarkus.http.port";

    public static final String FLIGHT_SERVICE_PORT_PROPERTY = "flight.quarkus.http.port";

    public static final String BOOKING_ID = "bookingId";
    public static final String SRA_ID = "sraId";
}
