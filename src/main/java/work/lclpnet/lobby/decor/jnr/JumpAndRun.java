package work.lclpnet.lobby.decor.jnr;

import it.unimi.dsi.fastutil.ints.IntFloatPair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import work.lclpnet.game.util.WorldModifier;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.FormatWrapper;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.mixin.ShulkerAccessor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JumpAndRun {

    private static final int DESTROYER_TIMEOUT_TICKS = 700;
    private static final int DESTROYER_DELAY_TICKS = 20;
    private final Random random = new Random();
    private final Map<Block, DyeColor> palette = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Blocks.DYED_TERRACOTTA::pick,
                    Function.identity()
            ));
    private final Block[] blockPalette = palette.keySet().toArray(Block[]::new);

    private final ServerLevel world;
    private final BlockPos start;
    private final Stack<BlockPos> nodes;
    private final PosGenerator generator;
    private final WorldModifier modifier;
    private final Translations translations;
    private PlayerTeam redTeam, greenTeam;
    private BlockPos next;
    private Shulker shulkerEntity;
    private int destroyerTimeout = DESTROYER_TIMEOUT_TICKS;
    private int destroyerDelay = 0;

    public JumpAndRun(ServerLevel world, LobbyWorldConfig config, WorldModifier modifier,
                      Scheduler scheduler, Translations translations) {
        this(world, config.jumpAndRunStart, modifier, scheduler, translations);
    }

    public JumpAndRun(ServerLevel world, BlockPos start, WorldModifier modifier, Scheduler scheduler,
                      Translations translations) {
        this.world = world;
        this.start = start;
        this.modifier = modifier;
        this.translations = translations;
        this.nodes = new Stack<>();
        this.nodes.push(start.below());

        final int maxY = world.getMaxY() + 1 - start.getY();  // max offset

        @SuppressWarnings("SuspiciousNameCombination")
        var config = new DefaultPosGenerator.Config(25, maxY - 25, List.of(
                // below offset dy, the chance to stay on the same height is reduced by p
                IntFloatPair.of(70, 0.85f),
                IntFloatPair.of(130, 0.75f),
                IntFloatPair.of(maxY, 0.6f)
        ));

        this.generator = new DefaultPosGenerator(world, nodes, config);

        setupTeams();

        startTask(scheduler);

        reset();
    }

    public void update(ServerPlayer player, Position position) {
        if (player.level() != world || !isNext(position)) return;

        next(player);
    }

    private boolean isNext(Position pos) {
        return (int) Math.floor(pos.x()) == next.getX() && (int) Math.floor(pos.y()) == next.getY() && (int) Math.floor(pos.z()) == next.getZ();
    }

    private void next(ServerPlayer player) {
        if (this.next.getY() >= world.getMaxY()) {
            this.collapse();
            this.win(player);
            return;
        }

        BlockPos next = generator.generate();
        if (next == null) {
            this.collapse();
            return;
        }

        // reset destroyer timeout
        destroyerTimeout = DESTROYER_TIMEOUT_TICKS;

        nodes.push(next);

        final Block block = randomBlock();

        modifier.setBlockState(next, block.defaultBlockState());

        world.playSound(null, next, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);

        spawnShulker(block, next, greenTeam);

        this.next = next.above();
    }

    private void win(ServerPlayer player) {
        var players = PlayerLookup.all(world.getServer());

        translations.translateText("lobby.jump_n_run.completed", FormatWrapper.styled(player.getScoreboardName(), ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .prefixed(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE))
                .sendTo(players);

        for (ServerPlayer p : players) {
            ServerPlayerAccess.playSoundToPlayer(p, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.RECORDS, 100f, 1f);
        }
    }

    private void collapse() {
        final BlockState air = Blocks.AIR.defaultBlockState();

        while (nodes.size() > 1) {
            BlockPos pos = nodes.pop();
            modifier.setBlockState(pos, air);
        }

        reset();
    }

    private void spawnShulker(Block block, BlockPos pos, PlayerTeam team) {
        if (shulkerEntity != null) {
            shulkerEntity.discard();
        }

        shulkerEntity = new Shulker(EntityTypes.SHULKER, world);
        shulkerEntity.setPos(Vec3.atLowerCornerOf(pos));
        shulkerEntity.setNoAi(true);
        shulkerEntity.setGlowingTag(true);
        shulkerEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 255, false, false, false));
        shulkerEntity.setSilent(true);
        shulkerEntity.setNoGravity(true);
        shulkerEntity.setInvulnerable(true);
        shulkerEntity.setInvisible(true);
        shulkerEntity.level().getScoreboard().addPlayerToTeam(shulkerEntity.getScoreboardName(), team);

        ((ShulkerAccessor) shulkerEntity).invokeSetVariant(dyeColor(block));

        modifier.spawnEntity(shulkerEntity);
    }

    private Block randomBlock() {
        return blockPalette[random.nextInt(blockPalette.length)];
    }

    private Optional<DyeColor> dyeColor(Block block) {
        return Optional.ofNullable(palette.get(block));
    }

    private void setupTeams() {
        ServerScoreboard scoreboard = world.getScoreboard();

        greenTeam = scoreboard.getPlayerTeam("jnr_green");
        if (greenTeam == null) {
            greenTeam = scoreboard.addPlayerTeam("jnr_green");
            greenTeam.setColor(Optional.of(TeamColor.GREEN));
        }

        redTeam = scoreboard.getPlayerTeam("jnr_red");
        if (redTeam == null) {
            redTeam = scoreboard.addPlayerTeam("jnr_red");
            redTeam.setColor(Optional.of(TeamColor.RED));
        }
    }

    private void startTask(Scheduler scheduler) {
        scheduler.interval(() -> {
            if (destroyerTimeout > 0) {
                destroyerTimeout--;
                return;
            }

            if (nodes.size() <= 1) return;  // no nodes to destroy (1st is start)

            int x = next.getX();
            int y = next.getY();
            int z = next.getZ();

            AABB box = new AABB(x - 1, y, z - 1, x + 2, y + 2, z + 2);

            List<ServerPlayer> nearbyPlayers = world.getEntitiesOfClass(ServerPlayer.class, box, p -> !p.isSpectator());

            if (!nearbyPlayers.isEmpty()) {
                next(nearbyPlayers.getFirst());
                return;
            }

            if (destroyerDelay > 0) {
                destroyerDelay--;
                return;
            }

            destroyerDelay = DESTROYER_DELAY_TICKS;

            BlockPos pos = nodes.pop();
            BlockPos last = nodes.peek();
            next = last.above();

            double centerX = x + 0.5;
            double centerY = y - 0.5;
            double centerZ = z + 0.5;

            modifier.setBlockState(pos, Blocks.AIR.defaultBlockState());
            world.sendParticles(ParticleTypes.FLAME, centerX, centerY, centerZ, 50, 0.25d, 0.25d, 0.25d, 0.1d);
            world.playSound(null, centerX, centerY, centerZ, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1f, 0f);

            if (nodes.size() > 1) {
                spawnShulker(world.getBlockState(last).getBlock(), last, redTeam);
            } else if (shulkerEntity != null) {
                shulkerEntity.discard();
            }
        }, 1);
    }

    private void reset() {
        nodes.clear();
        next = start;
        nodes.push(next.below());
        generator.reset();
        destroyerDelay = 0;
        destroyerTimeout = DESTROYER_TIMEOUT_TICKS;

        if (shulkerEntity != null) {
            shulkerEntity.discard();
        }
    }
}
