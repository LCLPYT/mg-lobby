package work.lclpnet.lobby.game.asset;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetPathTest {

    @Test
    void ofAndToStringWorks() {
        AssetPath path = AssetPath.of("a", "b");
        assertEquals("a/b", path.toString());
        assertArrayEquals(new String[]{"a", "b"}, path.segments());
    }

    @Test
    void ofWithSlashSplitsAutomatically() {
        AssetPath path = AssetPath.of("a/b", "c");
        assertEquals("a/b/c", path.toString());
        assertArrayEquals(new String[]{"a", "b", "c"}, path.segments());
    }

    @Test
    void resolveStringAndAssetPath() {
        AssetPath base = AssetPath.of("a", "b");
        assertEquals(AssetPath.of("a", "b", "c"), base.resolve("c"));
        assertEquals(AssetPath.of("a", "b", "x", "y"), base.resolve(AssetPath.of("x", "y")));
    }

    @Test
    void resolveEmptyStringReturnsSame() {
        AssetPath p = AssetPath.of("a");
        assertSame(p, p.resolve(""));
    }

    @Test
    void resolveRootDiscardsPrev() {
        AssetPath p = AssetPath.of("test", "/foo");
        assertEquals(AssetPath.of("foo"), p);

        p = AssetPath.of("test", "/", "foo");
        assertEquals(AssetPath.of("foo"), p);
    }

    @Test
    void resolveEmptySkipped() {
        AssetPath p = AssetPath.of("", "bar", "", "foo");
        assertEquals(AssetPath.of("bar", "foo"), p);
    }

    @Test
    void resolveParent() {
        AssetPath p = AssetPath.of("foo", "..", "bar");
        assertEquals(AssetPath.of("bar"), p);
    }

    @Test
    void resolveParentAtRoot() {
        AssetPath p = AssetPath.of("..", "bar");
        assertEquals("../bar", p.toString());

        p = AssetPath.of("../bar");
        assertEquals("../bar", p.toString());
    }

    @Test
    void resolveEmptyAssetPathReturnsSame() {
        AssetPath p = AssetPath.of("a");
        assertSame(p, p.resolve(AssetPath.of()));
    }

    @Test
    void parentAndResolveSibling() {
        AssetPath path = AssetPath.of("a", "b", "c");
        assertEquals(AssetPath.of("a", "b"), path.parent());
        assertEquals(AssetPath.of("a", "b", "x"), path.resolveSibling("x"));
        assertEquals(AssetPath.of("a", "b", "z"), path.resolveSibling(AssetPath.of("z")));
    }

    @Test
    void parentOfSingleSegmentIsEmpty() {
        assertTrue(AssetPath.of("a").parent().isEmpty());
    }

    @Test
    void toPathConvertsCorrectly() {
        AssetPath path = AssetPath.of("root", "folder", "file.txt");
        Path expected = Path.of("root", "folder", "file.txt");
        assertEquals(expected, path.toPath());
    }

    @Test
    void toPathOnEmptyThrows() {
        assertThrows(UnsupportedOperationException.class, () -> AssetPath.of().toPath());
    }

    @Test
    void equalityAndHashCode() {
        AssetPath p1 = AssetPath.of("a", "b");
        AssetPath p2 = AssetPath.of("a", "b");
        AssetPath p3 = AssetPath.of("a", "c");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(null, p1);
    }

    @Test
    void compareToOrdersByString() {
        AssetPath a = AssetPath.of("a");
        AssetPath b = AssetPath.of("b");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(AssetPath.of("a")));
    }
}