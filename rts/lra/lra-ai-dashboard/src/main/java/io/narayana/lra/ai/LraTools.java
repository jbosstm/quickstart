/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.ai;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * LangChain4j tool set for querying the Narayana LRA coordinator.
 *
 * Each method maps to one coordinator REST endpoint and carries a natural-language
 * description so the LLM knows when and why to call it.
 *
 * The LRA ID is always a full URI (e.g. http://host/lra-coordinator/<uid>),
 * so detail/status calls reduce to a plain GET on that URI.
 */
@ApplicationScoped
public class LraTools {

    @ConfigProperty(name = "lra.coordinator.url", defaultValue = "http://localhost:8080/lra-coordinator")
    String coordinatorUrl;

    private final HttpClient http = HttpClient.newHttpClient();

    @Tool("Returns all currently tracked LRAs — active, closing, cancelled, and recently completed — with their status, clientId, and recovery flag. Use this to get an overview of the coordinator state or count how many LRAs are in each lifecycle phase. IMPORTANT: FailedToCancel and FailedToClose LRAs are NOT included here — they are permanently moved to the failed store. Always call listFailedLRAs() in addition to this tool whenever checking for problems.")
    public String listAllLRAs() {
        return get(coordinatorUrl);
    }

    @Tool("Returns all LRAs in a specific lifecycle state. Valid states: Active, Closing, Closed, Cancelling, Cancelled, FailedToClose, FailedToCancel. Use this to narrow focus to problematic states such as FailedToClose or FailedToCancel.")
    public String listLRAsByStatus(
            @P("The LRA lifecycle state to filter by. One of: Active, Closing, Closed, Cancelling, Cancelled, FailedToClose, FailedToCancel") String status) {
        return get(coordinatorUrl + "?Status=" + status);
    }

    @Tool("Returns the complete record for a specific LRA: all participant URIs, per-participant status, timeout, top-level flag, and parent/child relationships. Use this to diagnose why a specific LRA is stuck or to inspect its participant list.")
    public String getLRADetails(
            @P("The full LRA ID, which is a URI such as http://host/lra-coordinator/<uid>") String lraId) {
        return get(lraId);
    }

    @Tool("Returns only the current lifecycle status string for a specific LRA (e.g. Cancelling, FailedToCancel). Cheaper than getLRADetails when you only need the status. Use this for quick status checks or to poll progress during multi-step diagnosis.")
    public String getLRAStatus(
            @P("The full LRA ID, which is a URI such as http://host/lra-coordinator/<uid>") String lraId) {
        return get(lraId + "/status");
    }

    @Tool("Returns all LRAs currently undergoing automatic recovery — transactions interrupted mid-flight that the recovery coordinator is retrying. Use this to understand recovery load and to confirm whether a given LRA is being retried.")
    public String listRecoveringLRAs() {
        return get(coordinatorUrl + "/recovery");
    }

    @Tool("Returns all LRAs in terminal failed states (FailedToClose, FailedToCancel) that the recovery coordinator cannot resolve automatically and that require manual operator intervention.")
    public String listFailedLRAs() {
        return get(coordinatorUrl + "/recovery/failed");
    }

    @Tool("Deletes the failed record of a FailedToCancel or FailedToClose LRA from the coordinator's ObjectStore, acknowledging that the heuristic has been resolved out-of-band. " +
            "This is the ONLY way to remove a failed LRA — calling cancelLRA or closeLRA on a terminal-failed LRA returns 412. " +
            "Only call this after the operator has confirmed that all participants have been manually compensated or completed. " +
            "The LRA ID must be the full URI (e.g. http://host/lra-coordinator/<uid>).")
    public String deleteFailedLRA(
            @P("The full LRA ID URI of the failed LRA to delete, e.g. http://host/lra-coordinator/<uid>") String lraId) {
        String uid = lraId.substring(lraId.lastIndexOf('/') + 1);
        return delete(coordinatorUrl + "/recovery/" + uid);
    }

    @Tool("Closes an LRA by asking all enrolled participants to complete their work (the happy-path end). Only call this when the operator explicitly requests it and after confirming the LRA ID. Returns the coordinator's response including the new LRA status.")
    public String closeLRA(
            @P("The full LRA ID URI to close, e.g. http://host/lra-coordinator/<uid>") String lraId) {
        return put(lraId + "/close");
    }

    @Tool("Cancels an LRA by asking all enrolled participants to compensate (roll back). Only call this when the operator explicitly requests it and after confirming the LRA ID. Returns the coordinator's response including the new LRA status.")
    public String cancelLRA(
            @P("The full LRA ID URI to cancel, e.g. http://host/lra-coordinator/<uid>") String lraId) {
        return put(lraId + "/cancel");
    }

    @Tool("Starts a new LRA and returns its full LRA ID URI. The new LRA is in Active state with no participants yet enrolled. " +
            "Only call this when the operator explicitly asks to create a new transaction.")
    public String startLRA(
            @P("A short descriptive name for this LRA, used to identify it in logs and listings (e.g. 'payment-saga-test'). Must not be empty.") String clientId,
            @P("Optional timeout in milliseconds after which the LRA is automatically cancelled if not closed. Use 0 for no timeout.") long timeLimitMs) {
        String url = coordinatorUrl + "/start?ClientID=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&TimeLimit=" + timeLimitMs;
        return post(url);
    }

    private String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return "Not found: " + url;
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error reaching coordinator at " + url + ": " + e.getMessage();
        }
    }

    private String put(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return "HTTP " + response.statusCode() + ": " + response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error reaching coordinator at " + url + ": " + e.getMessage();
        }
    }

    private String delete(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return "HTTP " + response.statusCode() + ": " + response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error reaching coordinator at " + url + ": " + e.getMessage();
        }
    }

    private String post(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "text/plain")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return "HTTP " + response.statusCode() + ": " + response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error reaching coordinator at " + url + ": " + e.getMessage();
        }
    }
}
