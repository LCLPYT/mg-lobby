package work.lclpnet.game.api.option.menu;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A named, cyclable order in which the options of a {@link work.lclpnet.game.impl.menu.PaginatedOptionMenu}
 * are displayed. Name based orders receive a viewer-bound name extractor at apply time, so the same
 * sort can be reused across players with different languages.
 *
 * @param <T> The option type.
 */
public interface OptionSort<T> {

    /**
     * @return The translation key of the label shown for this order in the menu header.
     */
    String labelKey();

    /**
     * Order the options.
     *
     * @param insertionOrdered The options in their insertion (default) order.
     * @param nameOf           Extracts the display name of an option for the current viewer.
     * @return The ordered options.
     */
    List<T> sort(List<T> insertionOrdered, Function<T, String> nameOf);

    static <T> OptionSort<T> of(String labelKey, BiFunction<List<T>, Function<T, String>, List<T>> sorter) {
        return new OptionSort<>() {
            @Override
            public String labelKey() {
                return labelKey;
            }

            @Override
            public List<T> sort(List<T> insertionOrdered, Function<T, String> nameOf) {
                return sorter.apply(insertionOrdered, nameOf);
            }
        };
    }

    /**
     * @return The default set of orders: insertion order, reversed insertion order and name (A-Z / Z-A).
     */
    static <T> List<OptionSort<T>> defaults() {
        return List.of(
                of("mg-api.menu.sort.default", (list, nameOf) -> list),
                of("mg-api.menu.sort.reverse", (list, nameOf) -> list.reversed()),
                of("mg-api.menu.sort.name_az", (list, nameOf) -> {
                    Comparator<T> byName = Comparator.comparing(option -> nameOf.apply(option).toLowerCase());
                    return list.stream().sorted(byName).toList();
                }),
                of("mg-api.menu.sort.name_za", (list, nameOf) -> {
                    Comparator<T> byName = Comparator.comparing(option -> nameOf.apply(option).toLowerCase());
                    return list.stream().sorted(byName.reversed()).toList();
                })
        );
    }
}
