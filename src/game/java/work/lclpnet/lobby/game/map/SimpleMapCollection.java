package work.lclpnet.lobby.game.map;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class SimpleMapCollection implements MapCollection {

    private final Map<ResourceLocation, GameMap> maps = new HashMap<>();

    @Override
    public void add(GameMap map) {
        ResourceLocation id = map.getDescriptor().getIdentifier();
        maps.put(id, map);
    }

    @Override
    public Collection<GameMap> getMaps() {
        return Collections.unmodifiableCollection(maps.values());
    }

    @Override
    public Optional<GameMap> getMap(ResourceLocation id) {
        return Optional.ofNullable(maps.get(id));
    }
}
