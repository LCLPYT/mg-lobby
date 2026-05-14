package work.lclpnet.lobby.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

    private HttpUtils() {}


    public static void badRequest(HttpExchange exchange) throws IOException {
        String html = """
                <html lang="en">
                <body>
                  <h1>Bad request</h1>
                </body>
                </html>
                """;

        writeHtml(exchange, 400, html);
    }

    public static void writeHtml(HttpExchange exchange, int code, String html) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        writeResponse(exchange, code, html.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeResponse(HttpExchange exchange, int code, byte[] bytes) throws IOException {
        try (var out = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(code, bytes.length);
            out.write(bytes);
        }
    }
}
