package work.lclpnet.lobby.game.map;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleMapCollectionTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void mapsWithPrefix() {
        var maps = new SimpleMapCollection();
        GameMap myFoo = new GameMap(new MapDescriptor("my_minigame", "my_map/foo", "1.20"));
        GameMap myBar = new GameMap(new MapDescriptor("my_minigame", "my_map/bar", "1.20"));
        GameMap otherFoo = new GameMap(new MapDescriptor("my_minigame", "other_map/foo", "1.20"));

        maps.add(myFoo);
        maps.add(myBar);
        maps.add(otherFoo);

        assertEquals(Set.of(myFoo, myBar),
                maps.mapsWithPrefix(new Identifier("my_minigame", "my_map")).collect(Collectors.toSet()));

        assertEquals(Set.of(otherFoo),
                maps.mapsWithPrefix(new Identifier("my_minigame", "other_map")).collect(Collectors.toSet()));

        assertEquals(Set.of(otherFoo, myFoo, myBar),
                maps.mapsWithPrefix(new Identifier("my_minigame", "")).collect(Collectors.toSet()));
    }

    @Test
    void mapIdsWithPrefix() {
        var maps = new SimpleMapCollection();
        maps.add(new GameMap(new MapDescriptor("my_minigame", "my_map/foo", "1.20")));
        maps.add(new GameMap(new MapDescriptor("my_minigame", "my_map/bar", "1.20")));
        maps.add(new GameMap(new MapDescriptor("my_minigame", "other_map/foo", "1.20")));

        assertEquals(Set.of(
                new Identifier("my_minigame", "my_map/foo"),
                new Identifier("my_minigame", "my_map/bar")
        ), maps.mapIdsWithPrefix(new Identifier("my_minigame", "my_map")).collect(Collectors.toSet()));

        assertEquals(Set.of(
                new Identifier("my_minigame", "other_map/foo")
        ), maps.mapIdsWithPrefix(new Identifier("my_minigame", "other_map")).collect(Collectors.toSet()));

        assertEquals(Set.of(
                new Identifier("my_minigame", "other_map/foo"),
                new Identifier("my_minigame", "my_map/foo"),
                new Identifier("my_minigame", "my_map/bar")
        ), maps.mapIdsWithPrefix(new Identifier("my_minigame", "")).collect(Collectors.toSet()));
    }
}