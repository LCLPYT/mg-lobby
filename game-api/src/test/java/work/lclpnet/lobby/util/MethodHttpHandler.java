package work.lclpnet.lobby.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class MethodHttpHandler implements HttpHandler {

    private final String method;
    private final HttpHandler child;

    public MethodHttpHandler(String method, HttpHandler child) {
        this.method = method;
        this.child = child;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!method.equals(exchange.getRequestMethod())) {
            HttpUtils.badRequest(exchange);
            return;
        }

        child.handle(exchange);
    }
}
