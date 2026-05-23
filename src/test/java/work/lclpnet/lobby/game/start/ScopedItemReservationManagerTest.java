package work.lclpnet.lobby.game.start;

import org.junit.jupiter.api.Test;
import work.lclpnet.game.api.start.ItemReservationManager.Reservation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScopedItemReservationManagerTest {

    @Test
    void reserveFirstFreeReturnsSlotZero() {
        var mgr = new ScopedItemReservationManager();
        var r = mgr.reserveFirstFree();
        assertNotNull(r);
        assertEquals(0, r.slot());
    }

    @Test
    void reserveFirstFreeReturnsAllNineSlots() {
        var mgr = new ScopedItemReservationManager();
        Set<Integer> slots = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            var r = mgr.reserveFirstFree();
            assertNotNull(r, "expected slot " + i + " to be available");
            assertTrue(slots.add(r.slot()), "duplicate slot " + r.slot());
        }
        assertEquals(9, slots.size());
    }

    @Test
    void reserveFirstFreeReturnsNullWhenFull() {
        var mgr = new ScopedItemReservationManager();
        for (int i = 0; i < 9; i++) mgr.reserveFirstFree();
        assertNull(mgr.reserveFirstFree());
    }

    @Test
    void reservePreferredSlotGrantsPreference() {
        var mgr = new ScopedItemReservationManager();
        var r = mgr.reserve(5);
        assertNotNull(r);
        assertEquals(5, r.slot());
    }

    @Test
    void reservePreferredSlotFallsBackWhenTaken() {
        var mgr = new ScopedItemReservationManager();
        mgr.reserve(5);
        var r = mgr.reserve(5);
        assertNotNull(r);
        assertNotEquals(5, r.slot());
        assertTrue(r.slot() >= 0 && r.slot() <= 8);
    }

    @Test
    void reserveWrapsAroundCorrectly() {
        var mgr = new ScopedItemReservationManager();
        // Fill slots 7 and 8
        mgr.reserve(7);
        mgr.reserve(8);
        // Preferred slot 7; next free wrapping around should be 0
        var r = mgr.reserve(7);
        assertNotNull(r);
        assertEquals(0, r.slot());
    }

    @Test
    void reserveInvalidSlotThrows() {
        var mgr = new ScopedItemReservationManager();
        assertThrows(IndexOutOfBoundsException.class, () -> mgr.reserve(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> mgr.reserve(9));
    }

    @Test
    void freeReservationReleasesSlot() {
        var mgr = new ScopedItemReservationManager();
        var r = mgr.reserve(3);
        assertNotNull(r);
        r.free();
        var r2 = mgr.reserve(3);
        assertNotNull(r2);
        assertEquals(3, r2.slot());
    }

    @Test
    void freeScopeReleasesAllReservations() {
        var mgr = new ScopedItemReservationManager();
        for (int i = 0; i < 9; i++) mgr.reserveFirstFree();
        assertNull(mgr.reserveFirstFree());

        mgr.free();

        var r = mgr.reserveFirstFree();
        assertNotNull(r);
    }

    @Test
    void subScopeSharesReservedSlots() {
        var parent = new ScopedItemReservationManager();
        var sub = parent.createSubScope();

        parent.reserve(2);
        // Slot 2 taken by parent, subscope should not get slot 2 as preferred
        var r = sub.reserve(2);
        assertNotNull(r);
        assertNotEquals(2, r.slot());
    }

    @Test
    void freeScopeDoesNotAffectParentReservations() {
        var parent = new ScopedItemReservationManager();
        parent.reserve(1);

        var sub = parent.createSubScope();
        sub.reserve(3);
        sub.free();

        // Slot 1 (parent) still taken, slot 3 (subscope) released
        var r1 = sub.reserve(1);
        assertNotNull(r1);
        assertNotEquals(1, r1.slot());

        var r3 = sub.reserve(3);
        assertNotNull(r3);
        assertEquals(3, r3.slot());
    }

    @Test
    void freeParentScopeDoesNotAffectSubScopeReservations() {
        var parent = new ScopedItemReservationManager();
        var sub = parent.createSubScope();

        sub.reserve(4);
        parent.free();

        // Slot 4 reserved by subscope should still be taken
        var r = parent.reserve(4);
        assertNotNull(r);
        assertNotEquals(4, r.slot());
    }

    @Test
    void tryReserveReturnsExactSlot() {
        var mgr = new ScopedItemReservationManager();
        var r = mgr.tryReserve(5);
        assertNotNull(r);
        assertEquals(5, r.slot());
    }

    @Test
    void tryReserveReturnsNullWhenTaken() {
        var mgr = new ScopedItemReservationManager();
        mgr.tryReserve(5);
        assertNull(mgr.tryReserve(5));
    }

    @Test
    void tryReserveInvalidSlotThrows() {
        var mgr = new ScopedItemReservationManager();
        assertThrows(IndexOutOfBoundsException.class, () -> mgr.tryReserve(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> mgr.tryReserve(9));
    }

    @Test
    void tryReserveFreeReleasesSlot() {
        var mgr = new ScopedItemReservationManager();
        var r = mgr.tryReserve(6);
        assertNotNull(r);
        r.free();
        var r2 = mgr.tryReserve(6);
        assertNotNull(r2);
        assertEquals(6, r2.slot());
    }

    @Test
    void tryReserveScopeDoesNotFreeParentSlot() {
        var parent = new ScopedItemReservationManager();
        parent.tryReserve(2);

        var sub = parent.createSubScope();
        assertNull(sub.tryReserve(2));

        parent.free();

        var r = sub.tryReserve(2);
        assertNotNull(r);
        assertEquals(2, r.slot());
    }

    @Test
    void freeParentScopeDoesNotReleaseTryReservedSubScopeSlot() {
        var parent = new ScopedItemReservationManager();
        var sub = parent.createSubScope();

        sub.tryReserve(7);
        parent.free();

        assertNull(parent.tryReserve(7));
        assertNull(sub.tryReserve(7));
    }

    @Test
    void concurrentReservationsProduceUniqueSlots() throws InterruptedException {
        var mgr = new ScopedItemReservationManager();
        int threads = 9;
        List<Reservation> results = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) results.add(null);

        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);

        for (int i = 0; i < threads; i++) {
            int idx = i;
            exec.submit(() -> {
                ready.countDown();
                try { go.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                results.set(idx, mgr.reserveFirstFree());
            });
        }

        ready.await();
        go.countDown();
        exec.shutdown();
        assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS));

        Set<Integer> slots = new HashSet<>();
        for (var r : results) {
            assertNotNull(r);
            assertTrue(slots.add(r.slot()), "duplicate slot " + r.slot());
        }
        assertEquals(9, slots.size());
    }
}
