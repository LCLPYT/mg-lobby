package work.lclpnet.lobby.util;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class TestHttpServer implements AutoCloseable {

    private final Logger logger;
    private final HttpServer server;

    private TestHttpServer(HttpServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public void start() {
        InetSocketAddress addr = server.getAddress();

        logger.info("Starting test http server on http://{}:{}", addr.getHostName(), addr.getPort());
        server.start();
        logger.info("Http server started");
    }

    @Override
    public void close() {
        logger.info("Stopping http server");
        server.stop(0);
        logger.info("Http server stopped");
    }

    public static Builder builder(Logger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final Logger logger;
        private final Map<String, HttpHandler> routes = new HashMap<>();
        private InetSocketAddress address;

        private Builder(Logger logger) {
            this.logger = logger;
        }

        public Builder address(InetSocketAddress address) {
            this.address = address;
            return this;
        }

        public Builder address(String hostname, int port) {
            return address(new InetSocketAddress(hostname, port));
        }

        public Builder route(String method, String path, HttpHandler handler) {
            this.routes.put(path, new MethodHttpHandler(method, handler));
            return this;
        }

        public TestHttpServer build() throws IOException {
            if (address == null) {
                address = new InetSocketAddress("hostname", 8000);
            }

            HttpServer server = HttpServer.create(address, 0);

            routes.forEach(server::createContext);

            return new TestHttpServer(server, logger);
        }
    }
}
