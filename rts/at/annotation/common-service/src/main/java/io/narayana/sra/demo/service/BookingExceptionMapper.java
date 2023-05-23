package io.narayana.sra.demo.service;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BookingExceptionMapper implements ExceptionMapper<BookingException> {
    @Override
    public Response toResponse(BookingException exception) {

        return Response.status(exception.getReason())
                .entity(exception.getMessage()).build();
    }
}
