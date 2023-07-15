package work.lclpnet.lobby.io.copy;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.tika.Tika;
import work.lclpnet.lobby.io.extract.ArchiveExtractor;
import work.lclpnet.lobby.io.extract.TarExtractor;
import work.lclpnet.lobby.io.extract.ZipExtractor;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class UrlWorldCopier implements WorldCopier {

    private final URL url;

    public UrlWorldCopier(URL url) {
        this.url = url;
    }

    @Override
    public void copyTo(Path path) throws IOException {
        final String mime = new Tika().detect(url);

        URLConnection connection = url.openConnection();

        ArchiveExtractor extractor = switch (mime) {
            case "application/zip" -> {
                // download zip file to tmp file
                Path tmp = Files.createTempFile("mg-lobby", "dl");

                try (var in = connection.getInputStream();
                     var out = Files.newOutputStream(tmp)) {

                    in.transferTo(out);
                }

                yield new ZipExtractor(tmp);
            }
            case "application/x-tar" -> new TarExtractor(connection.getInputStream());  // input streams will be closed by the TarExtractor
            case "application/gzip" -> new TarExtractor(new GZIPInputStream(connection.getInputStream()));
            case "application/x-xz" -> new TarExtractor(new XZCompressorInputStream(connection.getInputStream()));
            default ->
                    throw new UnsupportedOperationException("Copying files of mime type '%s' is not supported".formatted(mime));
        };

        extractor.extractTo(path);
    }
}
