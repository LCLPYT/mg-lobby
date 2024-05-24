package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
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

    default Stream<Identifier> mapsWithPrefix(Identifier prefix) {
        String str = prefix.toString();

        if (!str.endsWith("/") && str.charAt(str.length() - 1) != ':') {
            str = str + "/";
        }

        String prefixStr = str;

        return getMaps().stream()
                .map(GameMap::getDescriptor)
                .map(MapDescriptor::getIdentifier)
                .filter(id -> id.toString().startsWith(prefixStr))
                .distinct();
    }
}
