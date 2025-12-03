package org.worker.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Small HTTP helper utilities used across the orchestrator.
 * - async GET with logging
 * - URL builder that tolerates missing scheme or slashes
 */
public final class HttpUtils {

    private HttpUtils() {}

    private static HttpClient httpClient;

    public static synchronized HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
        }
        return httpClient;
    }

    public static String buildUrl(String host, String path) {
        if (host == null) host = "";
        String h = host;
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://" + h;
        }
        // ensure single slash join
        if (h.endsWith("/") && path.startsWith("/")) {
            return h.substring(0, h.length() - 1) + path;
        } else if (!h.endsWith("/") && !path.startsWith("/")) {
            return h + "/" + path;
        } else {
            return h + path;
        }
    }

    /**
     * Fire-and-forget async GET. Logs status and body when the response arrives.
     */
    public static void callGet(String urlStr) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            System.out.println("Sending HTTP GET " + urlStr);
            getHttpClient().sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .whenComplete((resp, ex) -> {
                        if (ex != null) {
                            System.err.println("Failed HTTP GET " + urlStr + " : " + ex.getMessage());
                        } else {
                            int code = resp.statusCode();
                            String body = resp.body() == null ? "" : resp.body();
                            System.out.println("HTTP GET " + urlStr + " -> " + code + " body=" + body);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Invalid URL for HTTP GET: " + urlStr + " : " + e.getMessage());
        }
    }

    public static HttpResponse GET(String urlStr) {
        HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
        HttpResponse<String> resp = null;
        try {
            resp = HttpUtils.getHttpClient().send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return resp;
    }
}