package com.kienminh.util;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientUtil {

    private static final String BASE_URL = "http://localhost:8080";

    /** Gửi request GET */
    public static String get(String path, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + path);
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();

                if (statusCode >= 200 && statusCode < 300) {
                    return entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                } else {
                    String body = entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                    System.err.println("GET " + path + " failed: " + statusCode + " - " + body);
                    return null;
                }
            });
        }
    }

    /** Gửi request POST */
    public static String post(String path, String json, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL + path);
            request.addHeader("Content-Type", "application/json");
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            // Chỉ set entity nếu json != null && !json.isEmpty()
            if (json != null && !json.isEmpty()) {
                request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            }

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();
                if (statusCode >= 200 && statusCode < 300) {
                    return entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                } else {
                    String body = entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                    System.err.println("POST " + path + " failed: " + statusCode + " - " + body);
                    return null;
                }
            });
        }
    }


    /** Gửi request DELETE */
    public static String delete(String path, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(BASE_URL + path);
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();

                if (statusCode >= 200 && statusCode < 300) {
                    return entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                } else {
                    String body = entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                    System.err.println("DELETE " + path + " failed: " + statusCode + " - " + body);
                    return null;
                }
            });
        }
    }

    /** Gửi request PUT */
    public static String put(String path, String json, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + path);
            request.addHeader("Content-Type", "application/json");
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            if (json == null || json.isEmpty()) {
                json = "{}";
            }
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            System.out.println("PUT " + path + " body: " + json);

            return client.execute(request, response -> {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();

                if (statusCode >= 200 && statusCode < 300) {
                    return entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                } else {
                    String body = entity != null
                            ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                            : "";
                    System.err.println("PUT " + path + " failed: " + statusCode + " - " + body);
                    return null;
                }
            });
        }
    }
}
