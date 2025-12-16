package work.lclpnet.lobby.game.map;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapDescriptorTest {

    @Test
    void createEmptyNamespace() {
        assertThrows(IllegalArgumentException.class, () -> new MapDescriptor("", "bbb"));
    }

    @Test
    void createEmptyPath() {
        new MapDescriptor("aa", "");
    }

    @Test
    void getIdentifier() {
        var desc = new MapDescriptor("foo", "bar");
        assertEquals(ResourceLocation.fromNamespaceAndPath("foo", "bar"), desc.getIdentifier());
    }

    @Test
    void getIdentifierNoPath() {
        var desc = new MapDescriptor("foo", "");
        assertEquals(ResourceLocation.fromNamespaceAndPath("foo", ""), desc.getIdentifier());
    }

    @Test
    void getMapPathFull() {
        var desc = new MapDescriptor("foo", "bar");
        assertEquals("foo/bar", desc.getMapPath());
    }

    @Test
    void getMapPathNamespaceOnly() {
        var desc = new MapDescriptor("foo", "");
        assertEquals("foo", desc.getMapPath());
    }

    @Test
    void resolveNoPath() {
        var desc = new MapDescriptor("foo", "").resolve("bar");
        assertEquals("foo/bar", desc.getMapPath());
    }

    @Test
    void resolveWithPath() {
        var desc = new MapDescriptor("foo", "bar").resolve("baz");
        assertEquals("foo/bar/baz", desc.getMapPath());
    }

    @Test
    void resolveAbsNamespaceOnly() {
        var desc = new MapDescriptor("foo", "").resolve("/baz");
        assertEquals("baz", desc.getMapPath());
    }

    @Test
    void resolveAbs() {
        var desc = new MapDescriptor("foo", "").resolve("/baz/bar");
        assertEquals("baz/bar", desc.getMapPath());
    }

    @Test
    void resolveAbsTrailingSlash() {
        var desc = new MapDescriptor("foo", "").resolve("/baz/");
        assertEquals("baz", desc.getMapPath());
    }
}
