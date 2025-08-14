package work.lclpnet.lobby.game.asset;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.String.join;
import static java.lang.System.arraycopy;

public class AssetPath implements Comparable<AssetPath> {

    private static final AssetPath EMPTY = new AssetPath(new String[0]);

    private final String[] segments;

    private AssetPath(String[] segments) {
        this.segments = segments;

        for (String segment : segments) {
            if (segment.indexOf('/') != -1) {
                throw new IllegalArgumentException("Segment '%s' contains a slash, which is disallowed");
            }
        }
    }

    public AssetPath resolve(String relative) {
        if (relative.isEmpty()) return this;

        String[] split = relative.split("/");

        return of(concatenated(segments, split));
    }

    public AssetPath resolve(AssetPath relative) {
        if (relative.segments.length == 0) return this;
        return of(concatenated(segments, relative.segments));
    }

    public AssetPath parent() {
        if (segments.length <= 1) return EMPTY;

        String[] sub = new String[segments.length - 1];
        arraycopy(segments, 0, sub, 0, sub.length);

        return ofChecked(sub);
    }

    public AssetPath resolveSibling(String relative) {
        return parent().resolve(relative);
    }

    public AssetPath resolveSibling(AssetPath relative) {
        return parent().resolve(relative);
    }

    public String[] segments() {
        return segments;
    }

    public Path toPath() {
        if (segments.length == 0) {
            throw new UnsupportedOperationException("Empty asset path cannot be converted to a path");
        }

        var sub = new String[segments.length - 1];
        arraycopy(segments, 1, sub, 0, sub.length);

        return Path.of(segments[0], sub);
    }

    public boolean isEmpty() {
        return segments.length == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AssetPath assetPath = (AssetPath) o;
        return Objects.deepEquals(segments, assetPath.segments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(segments);
    }

    @Override
    public String toString() {
        return join("/", segments);
    }

    @Override
    public int compareTo(@NotNull AssetPath o) {
        return toString().compareTo(o.toString());
    }

    private static String[] concatenated(String[] first, String[] second) {
        if (second.length == 0) return first;
        if (first.length == 0) return second;

        String[] newArr = new String[first.length + second.length];

        arraycopy(first, 0, newArr, 0, first.length);
        arraycopy(second, 0, newArr, first.length, second.length);

        return newArr;
    }

    public static AssetPath of(String... segments) {
        if (segments.length == 0) return EMPTY;

        List<String> list = new ArrayList<>();

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            if (segment.charAt(0) == '/') list.clear();

            for (String string : segment.split("/")) {
                if (string.isEmpty()) continue;

                // eagerly resolve parent directory, if possible
                if (string.equals("..") && !list.isEmpty() && !list.getLast().equals("..")) {
                    list.removeLast();
                    continue;
                }

                list.add(string);
            }
        }

        return ofChecked(list.toArray(new String[0]));
    }

    private static @NotNull AssetPath ofChecked(String[] filteredSegments) {
        if (filteredSegments.length == 0) return EMPTY;

        return new AssetPath(filteredSegments);
    }
}
