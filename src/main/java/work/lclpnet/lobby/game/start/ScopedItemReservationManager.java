package work.lclpnet.lobby.game.start;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.api.start.ItemReservationManager;

public class ScopedItemReservationManager implements ItemReservationManager {

    private final IntSet reserved;
    private final IntSet reservedInScope = new IntOpenHashSet();

    public ScopedItemReservationManager() {
        this.reserved = new IntOpenHashSet();
    }

    private ScopedItemReservationManager(IntSet reserved) {
        this.reserved = reserved;
    }

    @Override
    public @Nullable Reservation reserveFirstFree() {
        int slot = reserveFreeSlot(0);

        if (slot == -1) return null;

        return reservation(slot);
    }

    @Override
    public @Nullable Reservation reserve(int preferredSlot) {
        assertValidSlot(preferredSlot);

        int slot = reserveFreeSlot(preferredSlot);

        if (slot == -1) return null;

        return reservation(slot);
    }

    @Override
    public @Nullable Reservation tryReserve(int slot) {
        assertValidSlot(slot);

        synchronized (reserved) {
            if (reserved.contains(slot)) return null;

            reserved.add(slot);
            reservedInScope.add(slot);
        }

        return reservation(slot);
    }

    private void assertValidSlot(int slot) {
        if (slot < 0 || slot > 8) throw new IndexOutOfBoundsException("Slot must be in 0..8");
    }

    private Reservation reservation(int slot) {
        return new Reservation() {
            @Override
            public int slot() {
                return slot;
            }

            @Override
            public void free() {
                ScopedItemReservationManager.this.free(slot);
            }
        };
    }

    private int reserveFreeSlot(int start) {
        synchronized (reserved) {
            for (int i = 0; i < 9; i++) {
                int slot = (start + i) % 9;

                if (reserved.contains(slot)) continue;

                reserved.add(slot);
                reservedInScope.add(slot);

                return slot;
            }
        }

        return -1;
    }

    private void free(int slot) {
        synchronized (reserved) {
            if (reservedInScope.remove(slot)) {
                reserved.remove(slot);
            }
        }
    }

    public void free() {
        synchronized (reserved) {
            for (int i : reservedInScope) {
                reserved.remove(i);
            }

            reservedInScope.clear();
        }
    }

    /**
     * Creates a subscope of this item reservation manager.
     * The subscope shares the same reserved slots, changes to the parent scope will also be reflected in the subscope.
     * The subscope can be freed independently of the parent scope.
     */
    public ScopedItemReservationManager createSubScope() {
        return new ScopedItemReservationManager(reserved);
    }
}
