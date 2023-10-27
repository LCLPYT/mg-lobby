package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameMapTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testParseNoPathThrows() {
        JSONObject json = new JSONObject("""
                {
                  "icon": "minecraft:cobblestone",
                  "author": "LCLP"
                }
                """);

        assertThrows(IllegalArgumentException.class, () -> GameMap.parse(json.toMap(), new MapDescriptor("test", "", "")));
    }

    @Test
    void testParse() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map",
                  "icon": "minecraft:cobblestone",
                  "author": "LCLP"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "", "1.20"));

        assertEquals(new Identifier("test:my_map"), gameMap.getDescriptor().getIdentifier());
        assertEquals(Items.COBBLESTONE, gameMap.getIcon());
        assertEquals(Items.COBBLESTONE, gameMap.getProperty("icon"));
        assertEquals("LCLP", gameMap.getProperty("author"));
    }

    @Test
    void testMinimal() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "", "1.20"));

        assertEquals(new Identifier("test:my_map"), gameMap.getDescriptor().getIdentifier());
        assertEquals("test/my_map/1.20", gameMap.getDescriptor().getMapPath());
        assertEquals(GameMap.DEFAULT_ICON, gameMap.getIcon());
        assertEquals(GameMap.DEFAULT_ICON, gameMap.getProperty("icon"));
    }

    @Test
    void testPropertiesRemoved() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map",
                  "target": "target"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "", "1.20"));

        assertFalse(gameMap.getProperties().containsKey("path"));
        assertFalse(gameMap.getProperties().containsKey("target"));
    }

    @Test
    void testNested() {
        JSONObject json = new JSONObject("""
                {
                  "path": "my_map"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "nested", "1.20"));

        assertEquals(new Identifier("test:nested/my_map"), gameMap.getDescriptor().getIdentifier());
    }

    @Test
    void testNestedAbsoluteNamespaceOnly() {
        JSONObject json = new JSONObject("""
                {
                  "path": "/test"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "nested", "1.20"));

        assertEquals(new Identifier("test:"), gameMap.getDescriptor().getIdentifier());
    }

    @Test
    void testNestedAbsolute() {
        JSONObject json = new JSONObject("""
                {
                  "path": "/test/map_two"
                }
                """);

        GameMap gameMap = GameMap.parse(json.toMap(), new MapDescriptor("test", "nested", "1.20"));

        assertEquals(new Identifier("test:map_two"), gameMap.getDescriptor().getIdentifier());
        assertEquals("test/map_two/1.20", gameMap.getDescriptor().getMapPath());
    }
}
