package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public interface MapCollection extends Iterable<GameMap> {

    void add(GameMap map);

    Collection<GameMap> getMaps();

    Optional<GameMap> getMap(Identifier id);

    default void add(Collection<GameMap> maps) {
        maps.forEach(this::add);
    }

    @Nonnull
    @Override
    default Iterator<GameMap> iterator() {
        return getMaps().iterator();
    }

    default Stream<GameMap> mapsWithPrefix(Identifier prefix) {
        String str = prefix.toString();

        if (!str.endsWith("/") && str.charAt(str.length() - 1) != ':') {
            str = str + "/";
        }

        String prefixStr = str;

        Set<Identifier> seen = new HashSet<>();

        return getMaps().stream().filter(map -> {
            Identifier id = map.getDescriptor().getIdentifier();
            return id.toString().startsWith(prefixStr) && seen.add(id);
        });
    }

    default Stream<Identifier> mapIdsWithPrefix(Identifier prefix) {
        return mapsWithPrefix(prefix)
                .map(GameMap::getDescriptor)
                .map(MapDescriptor::getIdentifier);
    }
}
