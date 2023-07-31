package work.lclpnet.lobby.game.map;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GameMap {

    public static final Item DEFAULT_ICON = Items.GRASS_BLOCK;

    private final Identifier identifier;
    private final Item icon;
    private final Map<String, Object> properties;

    public GameMap(Identifier identifier, Item icon) {
        this(identifier, icon, Map.of());
    }

    public GameMap(Identifier identifier, Item icon, Map<String, Object> properties) {
        this.identifier = identifier;
        this.icon = icon;
        this.properties = properties;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Item getIcon() {
        return icon;
    }

    /**
     * Get a named property
     * @param name The property name
     * @return The property value, or null if there is no property with that name.
     * @param <T> The property type.
     * @throws ClassCastException If the requested property type is incompatible with the actual property type.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getProperty(String name) {
        Object o = properties.get(name);
        if (o == null) return null;

        return (T) o;
    }

    public static GameMap parse(JSONObject json, String namespace) {
        Identifier id = new Identifier(namespace, json.getString("id"));

        Item icon = null;

        if (json.has("icon")) {
            Identifier iconId = new Identifier(json.getString("icon"));
            icon = Registries.ITEM.get(iconId);
        }

        if (icon == null) {
            icon = DEFAULT_ICON;
        }

        Map<String, Object> properties = new HashMap<>();

        for (String key : json.keySet()) {
            properties.put(key, json.get(key));
        }

        properties.put("id", id);
        properties.put("icon", icon);

        return new GameMap(id, icon, properties);
    }
}
