package work.lclpnet.game.api.start;

import work.lclpnet.game.api.option.GameStartOptions;

public record GameStartArgs(
        GameStartOptions options,
        ItemReservationManager itemManager
) {}
