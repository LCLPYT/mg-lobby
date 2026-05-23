package work.lclpnet.game.api.start;

import org.jetbrains.annotations.Nullable;

/**
 * Manages item reservations.
 */
public interface ItemReservationManager {

    /**
     * Reserves the first free item slot.
     * @return A reservation for the first free item slot. Null if no slots are available.
     */
    @Nullable Reservation reserveFirstFree();

    /**
     * Tries to reserve a preferred slot.
     * If the preferred slot is unavailable, some other slot is still reserved if one is available.
     * The implementation may decide which slot to reserve in that case.
     * @param preferredSlot The preferred item slot.
     * @return A reservation for a free item slot. Prefers the requested slot. Null if no slots are available.
     */
    @Nullable Reservation reserve(int preferredSlot);

    /**
     * Tries to reserver a slot.
     * As opposed to {@link #reserve(int)}, if the slot is unavailable, null will be returned.
     * @param slot The slot.
     * @return A reservation for the slot, or null if unavailable.
     */
    @Nullable Reservation tryReserve(int slot);

    /**
     * A reservation for an item slot.
     * May be freed in order to make the slot available for reservation again.
     */
    interface Reservation {
        /**
         * Gets the reserved slot index.
         * @return The slot.
         */
        int slot();

        /**
         * Frees the reservation for this slot.
         * Consumers should no longer make use of the slot after calling this.
         */
        void free();
    }
}
