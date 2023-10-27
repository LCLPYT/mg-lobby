package work.lclpnet.lobby.game.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameMap {

    public static final Item DEFAULT_ICON = Items.GRASS_BLOCK;

    private final MapDescriptor descriptor;
    private final Item icon;
    private final Map<String, Object> properties;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock(), writeLock = lock.writeLock();

    public GameMap(MapDescriptor descriptor, Item icon) {
        this(descriptor, icon, Map.of());
    }

    public GameMap(MapDescriptor descriptor, Item icon, Map<String, Object> properties) {
        this.descriptor = descriptor;
        this.icon = icon;
        this.properties = new Object2ObjectOpenHashMap<>(properties);
    }

    public MapDescriptor getDescriptor() {
        return descriptor;
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
        final Object o;

        try {
            readLock.lock();
            o = properties.get(name);
        } finally {
            readLock.unlock();
        }

        if (o == null) return null;

        return (T) o;
    }

    public void putProperties(Map<String, Object> extra) {
        try {
            writeLock.lock();
            properties.putAll(extra);
        } finally {
            writeLock.unlock();
        }
    }

    public void putProperty(String key, Object value) {
        try {
            writeLock.lock();
            properties.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public static GameMap parse(Map<String, Object> properties, MapDescriptor parentDescriptor) {
        Object pathObj = properties.get("path");

        if (!(pathObj instanceof String path)) {
            throw new IllegalArgumentException("String property \"path\" is missing");
        }

        MapDescriptor descriptor = parentDescriptor.resolve(path);

        Item icon = null;

        Object iconObj = properties.get("icon");

        if (iconObj instanceof String iconStr) {
            Identifier iconId = new Identifier(iconStr);
            icon = Registries.ITEM.get(iconId);
        }

        if (icon == null || icon == Items.AIR) {
            icon = DEFAULT_ICON;
        }

        var props = new HashMap<String, Object>(properties.size());

        props.putAll(properties);

        props.put("icon", icon);

        props.remove("path");
        props.remove("target");

        return new GameMap(descriptor, icon, props);
    }
}
