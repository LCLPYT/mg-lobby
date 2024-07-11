package work.lclpnet.lobby.game.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameMap {

    public static final Item DEFAULT_ICON = Items.GRASS_BLOCK;

    private final MapDescriptor descriptor;
    private final Map<String, Object> properties;
    @Nullable
    private final MapDescriptor referrer;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock(), writeLock = lock.writeLock();
    private volatile Item icon = null;

    public GameMap(MapDescriptor descriptor) {
        this(descriptor, Map.of());
    }

    public GameMap(MapDescriptor descriptor, Map<String, Object> properties) {
        this(descriptor, properties, null);
    }

    public GameMap(MapDescriptor descriptor, Map<String, Object> properties, @Nullable MapDescriptor referrer) {
        this.descriptor = descriptor;
        this.properties = new Object2ObjectOpenHashMap<>(properties);
        this.referrer = referrer;
    }

    public MapDescriptor getDescriptor() {
        return descriptor;
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

    @NotNull
    public <T> T requireProperty(String name) {
        T prop = getProperty(name);
        return Objects.requireNonNull(prop, "Property \"%s\" is undefined".formatted(name));
    }

    public boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public boolean hasProperty(String name, Class<?> type) {
        Object prop = getProperty(name);

        if (prop == null) return false;

        return type.isInstance(prop);
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

    public Item getIcon() {
        if (icon != null) {
            return icon;
        }

        synchronized (this) {
            if (icon != null) return icon;

            Object iconObj = properties.get("icon");

            if (iconObj instanceof String iconStr) {
                Identifier iconId = new Identifier(iconStr);
                icon = Registries.ITEM.get(iconId);
            }

            if (icon == null || icon == Items.AIR) {
                icon = DEFAULT_ICON;
            }

            putProperty("icon", icon);
        }

        return icon;
    }

    public String getName() {
        return getName("en_us");
    }

    public String getName(String locale) {
        Object nameProp = getProperty("name");

        if (!(nameProp instanceof String name)) {
            return descriptor.getIdentifier().getPath();
        }

        Object translatedProp = getProperty("name-translated");

        if (!(translatedProp instanceof JSONObject mapping) || !mapping.has(locale)) return name;

        String translatedName = mapping.getString(locale);

        if (translatedName == null) return name;

        return translatedName;
    }

    public List<String> getAuthors() {
        Object authorProp = getProperty("author");
        Object authorsProp = getProperty("authors");

        String author = authorProp instanceof String ? (String) authorProp : null;

        if (!(authorsProp instanceof Iterable<?> iterable)) {
            return author != null ? List.of(author) : List.of();
        }

        List<String> list = new ArrayList<>();

        if (author != null) {
            list.add(author);
        }

        for (Object entry : iterable) {
            if (entry instanceof String str) {
                list.add(str);
            }
        }

        return Collections.unmodifiableList(list);
    }

    @Override
    public String toString() {
        return "GameMap{descriptor=%s}".formatted(descriptor);
    }

    public static GameMap parse(Map<String, Object> properties, MapDescriptor parentDescriptor) {
        Object pathObj = properties.get("path");

        if (!(pathObj instanceof String path)) {
            throw new IllegalArgumentException("String property \"path\" is missing");
        }

        MapDescriptor descriptor = parentDescriptor.resolve(path);

        var props = new HashMap<String, Object>(properties.size());

        props.putAll(properties);

        props.remove("path");
        props.remove("target");

        return new GameMap(descriptor, props, parentDescriptor);
    }

    public boolean isFrom(String idPrefix) {
        String id = descriptor.getIdentifier().toString();

        if (id.startsWith(idPrefix)) {
            return true;
        }

        if (referrer == null) {
            return false;
        }

        id = referrer.getIdentifier().toString();

        return id.startsWith(idPrefix);
    }
}
