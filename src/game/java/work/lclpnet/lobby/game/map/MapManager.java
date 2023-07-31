package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

public class MapManager {

    private final MapRepository mapRepository;
    private final Map<Identifier, GameMap> maps = new HashMap<>();
    private final Logger logger;

    public MapManager(MapRepository mapRepository, Logger logger) {
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
}
