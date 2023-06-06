package com.arjuna.mw.wst.client;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;

import com.arjuna.webservices.wscoor.CoordinationConstants;

/**
 * The class is used to perform WS-Transaction context insertion
 * and extraction for application level SOAP messages using JaxRPC.
 *
 */

public class JaxRPCHeaderContextProcessor extends JaxBaseHeaderContextProcessor implements Handler
{
    /**
     * The handler information.
     */
    private HandlerInfo handlerInfo ;

    /**
     * Initialise the handler information.
     * @param handlerInfo The handler information.
     */
    public void init(final HandlerInfo handlerInfo)
    {
        this.handlerInfo = handlerInfo ;
    }

    /**
     * Destroy the handler.
     */
    public void destroy()
    {
    }

    /**
     * Get the headers.
     * @return the headers.
     */
    public QName[] getHeaders()
    {
		return new QName[] {new QName(CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT)};
    }

    /**
     * Handle the request.
     * @param messageContext The current message context.
     */

    public boolean handleRequest(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        return handleOutboundMessage(soapMessage);
    }

    /**
     * Handle the response.
     * @param messageContext The current message context.
     */
    public boolean handleResponse(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        resumeTransaction(soapMessageContext.getMessage()) ;
        return true ;
    }

    /**
     * Handle the fault.
     * @param messageContext The current message context.
     */
    public boolean handleFault(final MessageContext messageContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)messageContext ;
        resumeTransaction(soapMessageContext.getMessage()) ;
        return true ;
    }
}