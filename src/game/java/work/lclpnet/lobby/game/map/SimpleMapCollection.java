package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import java.util.*;

public class SimpleMapCollection implements MapCollection {

    private final Map<Identifier, GameMap> maps = new HashMap<>();

    @Override
    public void add(GameMap map) {
        Identifier id = map.getDescriptor().getIdentifier();
        maps.put(id, map);
    }

    @Override
    public Collection<GameMap> getMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

    @Override
    public Optional<GameMap> getMap(Identifier id) {
        return Optional.ofNullable(maps.get(id));
    }
}
