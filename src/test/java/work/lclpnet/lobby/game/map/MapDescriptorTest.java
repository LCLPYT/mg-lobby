package work.lclpnet.lobby.game.map;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapDescriptorTest {

    @Test
    void createEmptyNamespace() {
        assertThrows(IllegalArgumentException.class, () -> new MapDescriptor("", "bbb", "aaa"));
    }

    @Test
    void createEmptyPath() {
        new MapDescriptor("aa", "", "cc");
    }

    @Test
    void createEmptyVersion() {
        new MapDescriptor("aa", "bb", "");
    }

    @Test
    void getIdentifier() {
        var desc = new MapDescriptor("foo", "bar", "20");
        assertEquals(new Identifier("foo", "bar"), desc.getIdentifier());
    }

    @Test
    void getIdentifierNoPath() {
        var desc = new MapDescriptor("foo", "", "20");
        assertEquals(new Identifier("foo", ""), desc.getIdentifier());
    }

    @Test
    void getVersion() {
        var desc = new MapDescriptor("foo", "bar", "20");
        assertEquals("20", desc.getVersion());
    }

    @Test
    void getVersionEmpty() {
        var desc = new MapDescriptor("foo", "bar", "");
        assertEquals("", desc.getVersion());
    }

    @Test
    void getMapPathFull() {
        var desc = new MapDescriptor("foo", "bar", "20");
        assertEquals("foo/bar/20", desc.getMapPath());
    }

    @Test
    void getMapPathNamespaceOnly() {
        var desc = new MapDescriptor("foo", "", "");
        assertEquals("foo", desc.getMapPath());
    }

    @Test
    void getMapPathNoVersion() {
        var desc = new MapDescriptor("foo", "bar", "");
        assertEquals("foo/bar", desc.getMapPath());
    }

    @Test
    void resolveNoPath() {
        var desc = new MapDescriptor("foo", "", "").resolve("bar");
        assertEquals("foo/bar", desc.getMapPath());
    }

    @Test
    void resolveWithPath() {
        var desc = new MapDescriptor("foo", "bar", "").resolve("baz");
        assertEquals("foo/bar/baz", desc.getMapPath());
    }

    @Test
    void resolveAbsNamespaceOnly() {
        var desc = new MapDescriptor("foo", "", "").resolve("/baz");
        assertEquals("baz", desc.getMapPath());
    }

    @Test
    void resolveAbs() {
        var desc = new MapDescriptor("foo", "", "").resolve("/baz/bar");
        assertEquals("baz/bar", desc.getMapPath());
    }

    @Test
    void resolveAbsTrailingSlash() {
        var desc = new MapDescriptor("foo", "", "").resolve("/baz/");
        assertEquals("baz", desc.getMapPath());
    }

    @Test
    void resolveAbsNamespaceVersion() {
        var desc = new MapDescriptor("foo", "", "20").resolve("/baz");
        assertEquals("baz/20", desc.getMapPath());
    }

    @Test
    void resolveAbsVersion() {
        var desc = new MapDescriptor("foo", "", "20").resolve("/baz/bar");
        assertEquals("baz/bar/20", desc.getMapPath());
    }
}
