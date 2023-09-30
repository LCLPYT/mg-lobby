package work.lclpnet.lobby.io;

import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.io.copy.UrlWorldCopier;
import work.lclpnet.lobby.util.TestHttpServer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static work.lclpnet.lobby.io.DirectoryWorldCopierTest.assertCopyCorrect;
import static work.lclpnet.lobby.io.DirectoryWorldCopierTest.assertIsTestContent;

class UrlWorldCopierTest {

    private static final Path TEST_RESOURCES = Path.of("src", "test", "resources");
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlWorldCopierTest.class);

    @Test
    void testHttpZip() throws IOException {
        Path dst = Files.createTempDirectory("mgl_uwc");

        try (var files = Files.list(dst)) {
            assertTrue(files.findAny().isEmpty());
        }

        var builder = TestHttpServer.builder(LOGGER)
                .address("localhost", 8000)
                .route("GET", "/dl/lobby-dl", exchange -> {
                    LOGGER.info("{} {} {}", exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getRequestURI());
                    Path file = TEST_RESOURCES.resolve("directory.zip");
                    long size = Files.size(file);

                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.add("Content-Type", "application/zip");
                    responseHeaders.add("Content-Disposition", "attachment; filename=directory.zip");

                    try (var out = exchange.getResponseBody();
                         var in = Files.newInputStream(file)) {

                        exchange.sendResponseHeaders(200, size);
                        in.transferTo(out);
                    }
                });

        try (TestHttpServer server = builder.build()) {
            server.start();

            URL url = new URL("http://localhost:8000/dl/lobby-dl");

            new UrlWorldCopier(url).copyTo(dst);
        }

        assertIsTestContent(dst);
    }

    @Test
    void testLocalZip() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.zip").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }

    @Test
    void testLocalTar() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.tar").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }

    @Test
    void testLocalTarGz() throws IOException {
        URL url = TEST_RESOURCES.resolve("directory.tar.gz").toUri().toURL();
        Path dst = Files.createTempDirectory("mgl_uwc");

        assertCopyCorrect(new UrlWorldCopier(url), dst);
    }
}