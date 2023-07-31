package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameMapTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testParse() {
        JSONObject json = new JSONObject("""
                {
                  "id": "my_map",
                  "icon": "minecraft:cobblestone",
                  "author": "LCLP"
                }
                """);

        GameMap gameMap = GameMap.parse(json, "test");

        assertEquals(new Identifier("test:my_map"), gameMap.getIdentifier());
        assertEquals(new Identifier("test:my_map"), gameMap.getProperty("id"));
        assertEquals(Items.COBBLESTONE, gameMap.getIcon());
        assertEquals(Items.COBBLESTONE, gameMap.getProperty("icon"));
        assertEquals("LCLP", gameMap.getProperty("author"));
    }

    @Test
    void testMinimal() {
        JSONObject json = new JSONObject("""
                {
                  "id": "my_map"
                }
                """);

        GameMap gameMap = GameMap.parse(json, "test");

        assertEquals(new Identifier("test:my_map"), gameMap.getIdentifier());
        assertEquals(new Identifier("test:my_map"), gameMap.getProperty("id"));
        assertEquals(GameMap.DEFAULT_ICON, gameMap.getIcon());
        assertEquals(GameMap.DEFAULT_ICON, gameMap.getProperty("icon"));
    }
}
