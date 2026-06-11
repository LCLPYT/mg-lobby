package work.lclpnet.game.api;

public interface WorldOptions {

    /**
     * @return Whether the map should be deleted after it was switched for another.
     */
    boolean shouldBeDeleted();

    /**
     * @return Whether an existing instance of the map should be deleted first before loading a clean version.
     */
    boolean isCleanMapRequired();

    /**
     * Controls whether players are teleported to the new world.
     * Please note that if this is false and when switching from a world that should be deleted with {@link #shouldBeDeleted()}, any players still remaining in the old world will be teleported to the overworld automatically.
     * Consumers calling this without teleporting players are thus required to manage the teleportation themselves.
     * @return Whether players should be teleported to the newly loaded map.
     */
    default boolean shouldTeleportPlayers() {
        return true;
    }

    /**
     * Open a clean new world and delete it after it was switched.
     */
    WorldOptions TEMPORARY = new WorldOptions() {
        @Override
        public boolean shouldBeDeleted() {
            return true;
        }

        @Override
        public boolean isCleanMapRequired() {
            return true;
        }
    };

    /**
     * Open a reusable map will not be deleted after it was switched.
     * When trying to open the map again, the old loaded instance will be reused.
     * However, all maps are deleted after the game ends.
     */
    WorldOptions REUSABLE = new WorldOptions() {
        @Override
        public boolean shouldBeDeleted() {
            return false;
        }

        @Override
        public boolean isCleanMapRequired() {
            return false;
        }
    };
}
