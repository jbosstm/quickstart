/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.ai;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

/**
 * JAX-RS endpoint exposing the LRA AI assistant as a streaming chat API.
 *
 * POST /chat  {"message": "Why is LRA abc-123 stuck?"}
 *          → chunked text/plain stream of tokens
 *
 * Plain TEXT_PLAIN is used rather than SSE: RESTEasy Reactive streams each
 * Multi<String> item as a chunk immediately, and the client appends raw bytes
 * with no format-stripping. SSE would require the client to parse data: lines
 * and re-join newlines split by the SSE encoder, causing spaces and line breaks
 * to be lost.
 *
 * No @Blocking is needed: Multi<String> is reactive and RESTEasy Reactive handles
 * it natively without blocking the I/O thread.
 */
@Path("/chat")
@ApplicationScoped
public class LraAiChatResource {

    private static final Logger log = Logger.getLogger(LraAiChatResource.class);

    @Inject
    LraAssistant assistant;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> chat(ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new BadRequestException("Message must not be empty");
        }
        return assistant.chat(request.message())
                .onFailure().recoverWithItem(t -> {
                    log.errorf(t, "LLM call failed");
                    return "\n\n[Error: " + t.getMessage() + "]";
                });
    }

    public record ChatRequest(String message) {}
}
