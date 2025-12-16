package work.lclpnet.lobby.game.map;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public interface MapCollection extends Iterable<GameMap> {

    void add(GameMap map);

    Collection<GameMap> getMaps();

    Optional<GameMap> getMap(ResourceLocation id);

    default void add(Collection<GameMap> maps) {
        maps.forEach(this::add);
    }

    @NotNull
    @Override
    default Iterator<GameMap> iterator() {
        return getMaps().iterator();
    }

    default Stream<GameMap> mapsWithPrefix(ResourceLocation prefix) {
        String str = prefix.toString();

        if (!str.endsWith("/") && str.charAt(str.length() - 1) != ':') {
            str = str + "/";
        }

        String prefixStr = str;

        Set<ResourceLocation> seen = new HashSet<>();

        return getMaps().stream()
                .filter(map -> map.isFrom(prefixStr) && seen.add(map.getDescriptor().getIdentifier()));
    }

    default Stream<ResourceLocation> mapIdsWithPrefix(ResourceLocation prefix) {
        return mapsWithPrefix(prefix)
                .map(GameMap::getDescriptor)
                .map(MapDescriptor::getIdentifier);
    }
}
