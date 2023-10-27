package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

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
}
