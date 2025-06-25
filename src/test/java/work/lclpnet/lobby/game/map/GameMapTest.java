package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameMapTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void parseNoPathThrows() {
        JSONObject json = new JSONObject("""
                {
                  "icon": "minecraft:cobblestone",
                  "author": "LCLP"
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> GameMap.parse(json, new MapDescriptor("test", "")));
    }

    @Test
    void parse() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map",
                  "icon": "minecraft:cobblestone",
                  "author": "LCLP"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", ""));

        assertEquals(Identifier.of("test:my_map"), gameMap.getDescriptor().getIdentifier());
        assertEquals("minecraft:cobblestone", gameMap.getProperty("icon"));
        assertEquals("LCLP", gameMap.getProperty("author"));
    }

    @Test
    void parseMinimal() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", ""));

        assertEquals(Identifier.of("test:my_map"), gameMap.getDescriptor().getIdentifier());
        assertEquals("test/my_map", gameMap.getDescriptor().getMapPath());
        assertNull(gameMap.getProperty("icon"));
    }

    @Test
    void parsePropertiesRemoved() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map",
                  "target": "target"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", ""));

        assertFalse(gameMap.getProperties().has("path"));
        assertFalse(gameMap.getProperties().has("target"));
    }

    @Test
    void parseNested() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", "nested"));

        assertEquals(Identifier.of("test:nested/my_map"), gameMap.getDescriptor().getIdentifier());
    }

    @Test
    void parseNestedAbsoluteNamespaceOnly() {
        JSONObject json = new JSONObject("""
                {
                  "path": "/test"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", "nested"));

        assertEquals(Identifier.of("test:"), gameMap.getDescriptor().getIdentifier());
    }

    @Test
    void parseNestedAbsolute() {
        JSONObject json = new JSONObject("""
                {
                  "path": "/test/map_two"
                }
                """);

        GameMap gameMap = GameMap.parse(json, new MapDescriptor("test", "nested"));

        assertEquals(Identifier.of("test:map_two"), gameMap.getDescriptor().getIdentifier());
        assertEquals("test/map_two", gameMap.getDescriptor().getMapPath());
    }

    @Test
    void getAuthorsArray() {
        JSONObject json = new JSONObject("""
                {
                    "authors": [
                      "foo",
                      "bar"
                    ]
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals(List.of("foo", "bar"), gameMap.getAuthors());
    }

    @Test
    void getAuthorsUndefined() {
        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"));

        assertEquals(List.of(), gameMap.getAuthors());
    }

    @Test
    void getAuthorsAuthor() {
        JSONObject json = new JSONObject("""
                {
                    "author": "foo"
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals(List.of("foo"), gameMap.getAuthors());
    }

    @Test
    void getAuthorsAuthorMergedWithAuthors() {
        JSONObject json = new JSONObject("""
                {
                    "author": "baz",
                    "authors": [
                      "foo",
                      "bar"
                    ]
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals(List.of("baz", "foo", "bar"), gameMap.getAuthors());
    }

    @Test
    void getNameUndefined() {
        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"));

        assertEquals("nested", gameMap.getName());
    }

    @Test
    void getNameLocalized() {
        JSONObject json = new JSONObject("""
                {
                    "name": "Custom Name",
                    "name-translated": {
                      "de_de": "Spezieller Name"
                    }
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals("Custom Name", gameMap.getName());
        assertEquals("Custom Name", gameMap.getName("en_us"));
        assertEquals("Spezieller Name", gameMap.getName("de_de"));
        assertEquals("Custom Name", gameMap.getName("smth_else"));
    }

    @Test
    void getIcon() {
        JSONObject json = new JSONObject("""
                {
                    "icon": "minecraft:diamond"
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals(Items.DIAMOND, gameMap.getIcon());
        assertEquals(Items.DIAMOND, gameMap.getProperty("icon"));
    }

    @Test
    void getIconUndefined() {
        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"));

        assertEquals(GameMap.DEFAULT_ICON, gameMap.getIcon());
    }

    @Test
    void getIconUnknown() {
        JSONObject json = new JSONObject("""
                {
                    "icon": "_zzzzzzzzzzzzzzz:zzzzzzzzzzzzz"
                }
                """);

        GameMap gameMap = new GameMap(new MapDescriptor("test", "nested"), json);

        assertEquals(GameMap.DEFAULT_ICON, gameMap.getIcon());
    }

    private static Map<String, Object> entries(JSONObject json) {
        Map<String, Object> entries = new HashMap<>();

        for (String key : json.keySet()) {
            entries.put(key, json.get(key));
        }

        return entries;
    }
}
