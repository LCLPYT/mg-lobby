package work.lclpnet.lobby.util;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

public class JsonAssertions {

    public static void assertSimilar(JSONObject expected, JSONObject actual) {
        if (actual.similar(expected)) return;

        Assertions.assertEquals(expected, actual);
    }
}
