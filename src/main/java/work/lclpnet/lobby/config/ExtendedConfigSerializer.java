package work.lclpnet.lobby.config;

import org.json.JSONObject;
import org.slf4j.Logger;
import work.lclpnet.config.json.FileConfigSerializer;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ExtendedConfigSerializer<T extends JsonConfig> extends FileConfigSerializer<T> {

    /**
     * Create a new FileConfigSerializer.
     *
     * @param factory A {@link JsonConfigFactory} that converts {@link org.json.JSONObject}s to configs and provides a default config.
     * @param logger  A logger for error logging.
     */
    public ExtendedConfigSerializer(JsonConfigFactory<T> factory, Logger logger) {
        super(factory, logger);
    }

    @Override
    public void saveConfig(T config, Path file) throws IOException {
        JSONObject json = config.toJson();
        String content = json.toString(2);

        // prettify BlockPos tuples like [[x1, y1, z1], [x2, y2, z2]]
        Pattern tuplePattern = Pattern.compile("\\[\\s*(?:\\[(?:\\s*-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?,?){3}\\s*],?\\s*){2}]");

        content = tuplePattern.matcher(content).replaceAll(matchResult -> {
            final String match = matchResult.group();

            // remove all whitespace and put space after each comma
            return match.replaceAll("\\s+", "").replaceAll(",", ", ");
        });

        Path dir = file.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
