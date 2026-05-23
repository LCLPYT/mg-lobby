package work.lclpnet.game.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import work.lclpnet.kibu.access.misc.CustomNbt;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;

public record VotingHandler<T>(Voting<T> voting) {

    public static final MapCodec<String> MAP_CODEC = Codec.STRING.fieldOf("mg-api.voting");

    public void init(HookRegistrar hooks) {
        PlayerInteractionHooks.USE_ITEM.registerWith(hooks, (player, _, hand) -> {
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);

            if (CustomNbt.get(stack, MAP_CODEC)
                    .filter(voting.getId()::equals)
                    .isPresent()) {
                voting.open(sp);
                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });

        PlayerConnectionHooks.QUIT.registerWith(hooks, voting::removeVote);
    }

    public void giveStackToNewPlayers(HookRegistrar hooks, int slot) {
        PlayerConnectionHooks.JOIN.registerWith(hooks, player -> giveStackTo(player, slot));
    }

    public void giveStackTo(ServerPlayer player, int slot) {
        ItemStack stack = voting.getData().icon().apply(player);

        CustomNbt.set(stack, MAP_CODEC, voting.getId());

        player.getInventory().setItem(slot, stack);
    }

    public void giveStackTo(Iterable<? extends ServerPlayer> players, int slot) {
        for (ServerPlayer player : players) {
            giveStackTo(player, slot);
        }
    }
}
