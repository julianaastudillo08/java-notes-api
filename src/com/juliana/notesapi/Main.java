package com.juliana.notesapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java Notes API — HTTP server stdlib (sin frameworks).
 * Compilar: javac src/com/juliana/notesapi/Main.java
 * Ejecutar: java -cp src com.juliana.notesapi.Main
 */
public class Main {
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final Map<Integer, Note> NOTES = new ConcurrentHashMap<>();

    record Note(int id, String title, String body, String createdAt) {}

    public static void main(String[] args) throws IOException {
        NOTES.put(1, new Note(1, "Bienvenida", "API Java lista para demos de portafolio", Instant.now().toString()));
        SEQ.set(1);

        HttpServer server = HttpServer.create(new InetSocketAddress(4140), 0);
        server.createContext("/health", Main::health);
        server.createContext("/notes", Main::notes);
        server.setExecutor(null);
        System.out.println("java-notes-api → http://localhost:4140");
        server.start();
    }

    private static void health(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            send(ex, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        send(ex, 200, "{\"status\":\"ok\",\"service\":\"java-notes-api\",\"lang\":\"java\"}");
    }

    private static void notes(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if ("GET".equals(method) && "/notes".equals(path)) {
            List<String> items = new ArrayList<>();
            for (Note n : NOTES.values()) {
                items.add(toJson(n));
            }
            send(ex, 200, "[" + String.join(",", items) + "]");
            return;
        }

        if ("POST".equals(method) && "/notes".equals(path)) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String title = extract(body, "title");
            String text = extract(body, "body");
            if (title.isBlank()) {
                send(ex, 400, "{\"error\":\"title required\"}");
                return;
            }
            int id = SEQ.incrementAndGet();
            Note n = new Note(id, title, text, Instant.now().toString());
            NOTES.put(id, n);
            send(ex, 201, toJson(n));
            return;
        }

        if ("DELETE".equals(method) && path.matches("/notes/\\d+")) {
            int id = Integer.parseInt(path.substring("/notes/".length()));
            if (NOTES.remove(id) == null) {
                send(ex, 404, "{\"error\":\"not found\"}");
                return;
            }
            send(ex, 204, "");
            return;
        }

        send(ex, 404, "{\"error\":\"not found\"}");
    }

    private static String extract(String json, String key) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return "";
        int colon = json.indexOf(':', i);
        int q1 = json.indexOf('"', colon + 1);
        int q2 = json.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) return "";
        return json.substring(q1 + 1, q2);
    }

    private static String toJson(Note n) {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"body\":\"%s\",\"createdAt\":\"%s\"}",
            n.id(),
            esc(n.title()),
            esc(n.body()),
            n.createdAt()
        );
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, code == 204 ? -1 : bytes.length);
        if (code != 204) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
        ex.close();
    }
}
