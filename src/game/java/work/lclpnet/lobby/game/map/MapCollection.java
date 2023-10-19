package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class MapCollection implements Iterable<GameMap> {

    private final MapRepository mapRepository;
    private final Map<Identifier, GameMap> maps = new HashMap<>();
    private final Logger logger;

    public MapCollection(MapRepository mapRepository, Logger logger) {
        this.mapRepository = mapRepository;
        this.logger = logger;
    }

    public void load(String namespace) throws IOException {
        Set<GameMap> maps = mapRepository.getMaps(namespace);

        for (GameMap map : maps) {
            Identifier id = map.getIdentifier();

            if (this.maps.containsKey(id)) {
                logger.warn("Duplicate map id '{}'", id);
                continue;
            }

            this.maps.put(id, map);
        }
    }

    public Collection<GameMap> getMaps() {
        return maps.values();
    }

    public Optional<GameMap> getMap(Identifier id) {
        return Optional.ofNullable(maps.get(id));
    }

    @Nonnull
    @Override
    public Iterator<GameMap> iterator() {
        return getMaps().iterator();
    }

    public void load(GameMap gameMap) throws IOException {
        var data = mapRepository.getData(gameMap.getIdentifier());

        gameMap.putProperties(data);
    }

    public Optional<URI> getWorldSource(GameMap gameMap) throws IOException {
        final String property = "source";

        Object source = gameMap.getProperty(property);

        if (source instanceof URI uri) {
            return Optional.of(uri);
        }

        load(gameMap);

        var mapSource = mapRepository.getMapSource(gameMap.getProperties(), gameMap.getIdentifier());

        mapSource.ifPresent(uri -> gameMap.putProperty(property, uri));

        return mapSource;
    }
}
