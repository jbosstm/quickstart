package com.jboss.jbosstm.xts.demo.theatre;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.0
 * 
 */
@WebService(name = "ITheatreServiceBA", targetNamespace = "http://www.jboss.com/jbosstm/xts/demo/Theatre")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ITheatreServiceBA {


    /**
     * 
     * @param howMany
     * @param whichArea
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(name = "bookSeatsBAResponse", partName = "bookSeatsBAResponse")
    public boolean bookSeats(
        @WebParam(name = "how_many", partName = "how_many")
        int howMany,
        @WebParam(name = "which_area", partName = "which_area")
        int whichArea);

}