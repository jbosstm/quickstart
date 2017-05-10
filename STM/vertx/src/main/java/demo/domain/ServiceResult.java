package demo.domain;

public class ServiceResult {
    private String serviceName;
    private String threadId;
    private String message;
    private int bookingCount;
    private int altBookingCount;

    public ServiceResult(String serviceName, String threadId, String message, int bookingCount, int altBookingCount) {
        this.serviceName = serviceName;
        this.threadId = threadId;
        this.message = message;
        this.bookingCount = bookingCount;
        this.altBookingCount = altBookingCount;
    }

    public ServiceResult(String serviceName, String threadId, int bookingCount) {
        this(serviceName, threadId, "", bookingCount, 0);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getMessage() {
        return message;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public int getAltBookingCount() {
        return altBookingCount;
    }
}
