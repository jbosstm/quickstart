package com.arjuna.mw.wst.service;

import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import jakarta.xml.soap.SOAPMessage;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxRPC.
 */
public class JaxRPCSubordinateHeaderContextProcessor extends JaxRPCHeaderContextProcessor implements Handler
{
    /**
     * Handle the request.
     * @param messageContext The current message context.
     */
    public boolean handleRequest(final MessageContext messageContext)
    {
		final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        // the generic handler can do the job for us -- just pass the correct flag

        return handleInboundMessage(soapMessage, false);
    }
}