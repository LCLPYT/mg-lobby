package work.lclpnet.game.api.option.menu;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionSortTest {

    private static final Function<String, String> NAME_OF = Function.identity();

    @Test
    void defaults_haveFourOrders() {
        assertEquals(4, OptionSort.<String>defaults().size());
    }

    @Test
    void defaultOrder_keepsInsertionOrder() {
        OptionSort<String> sort = OptionSort.<String>defaults().get(0);

        assertEquals("mg-api.menu.sort.default", sort.labelKey());
        assertEquals(List.of("c", "a", "b"), sort.sort(List.of("c", "a", "b"), NAME_OF));
    }

    @Test
    void reverseOrder_reversesInsertionOrder() {
        OptionSort<String> sort = OptionSort.<String>defaults().get(1);

        assertEquals("mg-api.menu.sort.reverse", sort.labelKey());
        assertEquals(List.of("b", "a", "c"), sort.sort(List.of("c", "a", "b"), NAME_OF));
    }

    @Test
    void nameAscending_sortsByNameIgnoringCase() {
        OptionSort<String> sort = OptionSort.<String>defaults().get(2);

        assertEquals("mg-api.menu.sort.name_az", sort.labelKey());
        assertEquals(List.of("Apple", "banana", "Cherry"),
                sort.sort(List.of("Cherry", "Apple", "banana"), NAME_OF));
    }

    @Test
    void nameDescending_sortsByNameIgnoringCase() {
        OptionSort<String> sort = OptionSort.<String>defaults().get(3);

        assertEquals("mg-api.menu.sort.name_za", sort.labelKey());
        assertEquals(List.of("Cherry", "banana", "Apple"),
                sort.sort(List.of("Cherry", "Apple", "banana"), NAME_OF));
    }
}
