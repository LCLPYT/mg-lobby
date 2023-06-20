package work.lclpnet.lobby.decor.jnr;

import it.unimi.dsi.fastutil.ints.IntFloatPair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.scheduler.api.TaskHandle;
import work.lclpnet.lobby.service.TranslationService;
import work.lclpnet.lobby.util.FormatWrapper;
import work.lclpnet.lobby.util.WorldModifier;

import java.util.*;

public class JumpAndRun {

    private static final int DESTROYER_TIMEOUT_TICKS = 700;
    private static final int DESTROYER_DELAY_TICKS = 20;
    private final Random random = new Random();
    private final Map<Block, DyeColor> palette = Map.ofEntries(
            Map.entry(Blocks.BLACK_TERRACOTTA, DyeColor.BLACK),
            Map.entry(Blocks.BLUE_TERRACOTTA, DyeColor.BLUE),
            Map.entry(Blocks.BROWN_TERRACOTTA, DyeColor.BROWN),
            Map.entry(Blocks.CYAN_TERRACOTTA, DyeColor.CYAN),
            Map.entry(Blocks.GRAY_TERRACOTTA, DyeColor.GRAY),
            Map.entry(Blocks.GREEN_TERRACOTTA, DyeColor.GREEN),
            Map.entry(Blocks.LIGHT_BLUE_TERRACOTTA, DyeColor.LIGHT_BLUE),
            Map.entry(Blocks.LIGHT_GRAY_TERRACOTTA, DyeColor.LIGHT_GRAY),
            Map.entry(Blocks.LIME_TERRACOTTA, DyeColor.LIME),
            Map.entry(Blocks.MAGENTA_TERRACOTTA, DyeColor.MAGENTA),
            Map.entry(Blocks.ORANGE_TERRACOTTA, DyeColor.ORANGE),
            Map.entry(Blocks.PINK_TERRACOTTA, DyeColor.PINK),
            Map.entry(Blocks.PURPLE_TERRACOTTA, DyeColor.PURPLE),
            Map.entry(Blocks.RED_TERRACOTTA, DyeColor.RED),
            Map.entry(Blocks.WHITE_TERRACOTTA, DyeColor.WHITE),
            Map.entry(Blocks.YELLOW_TERRACOTTA, DyeColor.YELLOW)
    );
    private final Block[] blockPalette = palette.keySet().toArray(Block[]::new);

    private final ServerWorld world;
    private final BlockPos start;
    private final Stack<BlockPos> nodes;
    private final PosGenerator generator;
    private final WorldModifier modifier;
    private final TranslationService translations;
    private Team redTeam, greenTeam;
    private BlockPos next;
    private ShulkerEntity shulkerEntity;
    private int destroyerTimeout = DESTROYER_TIMEOUT_TICKS;
    private int destroyerDelay = 0;

    public JumpAndRun(ServerWorld world, BlockPos start, WorldModifier modifier, Scheduler scheduler, TranslationService translations) {
        this.world = world;
        this.start = start;
        this.modifier = modifier;
        this.translations = translations;
        this.nodes = new Stack<>();
        this.nodes.push(start.down());

        final int maxY = world.getTopY() - start.getY();  // max offset

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

    public void update(ServerPlayerEntity player, Position position) {
        if (player.getWorld() != world || !isNext(position)) return;

        next(player);
    }

    private boolean isNext(Position pos) {
        return (int) Math.floor(pos.getX()) == next.getX() && (int) Math.floor(pos.getY()) == next.getY() && (int) Math.floor(pos.getZ()) == next.getZ();
    }

    private void next(ServerPlayerEntity player) {
        if (this.next.getY() >= world.getTopY()) {
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

        modifier.setBlockState(next, block.getDefaultState());

        world.playSound(null, next, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);

        spawnShulker(block, next, greenTeam);

        this.next = next.up();
    }

    private void win(ServerPlayerEntity player) {
        var players = PlayerLookup.all(world.getServer());

        translations.translateText("lobby.jump_n_run.completed", FormatWrapper.styled(player.getEntityName(), Formatting.YELLOW, Formatting.BOLD))
                .formatted(Formatting.GOLD, Formatting.BOLD)
                .prefixed(Text.literal("Lobby> ").formatted(Formatting.BLUE))
                .sendTo(players);

        for (ServerPlayerEntity p : players) {
            p.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.RECORDS, 100f, 1f);
        }
    }

    private void collapse() {
        final BlockState air = Blocks.AIR.getDefaultState();

        while (nodes.size() > 1) {
            BlockPos pos = nodes.pop();
            modifier.setBlockState(pos, air);
        }

        reset();
    }

    private void spawnShulker(Block block, BlockPos pos, Team team) {
        if (shulkerEntity != null) {
            shulkerEntity.discard();
        }

        shulkerEntity = new ShulkerEntity(EntityType.SHULKER, world);
        shulkerEntity.setPosition(Vec3d.of(pos));
        shulkerEntity.setAiDisabled(true);
        shulkerEntity.setGlowing(true);
        shulkerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 255, false, false, false));
        shulkerEntity.setSilent(true);
        shulkerEntity.setNoGravity(true);
        shulkerEntity.setInvulnerable(true);
        shulkerEntity.setVariant(dyeColor(block));
        shulkerEntity.setInvisible(true);
        shulkerEntity.getWorld().getScoreboard().addPlayerToTeam(shulkerEntity.getEntityName(), team);

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

        greenTeam = scoreboard.getTeam("jnr_green");
        if (greenTeam == null) {
            greenTeam = scoreboard.addTeam("jnr_green");
            greenTeam.setColor(Formatting.GREEN);
        }

        redTeam = scoreboard.getTeam("jnr_red");
        if (redTeam == null) {
            redTeam = scoreboard.addTeam("jnr_red");
            redTeam.setColor(Formatting.RED);
        }
    }

    private TaskHandle startTask(Scheduler scheduler) {
        return scheduler.interval(() -> {
            if (destroyerTimeout > 0) {
                destroyerTimeout--;
                return;
            }

            if (nodes.size() <= 1) return;  // no nodes to destroy (1st is start)

            int x = next.getX();
            int y = next.getY();
            int z = next.getZ();

            Box box = new Box(x - 1, y, z - 1, x + 2, y + 2, z + 2);

            List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, box, p -> !p.isSpectator());

            if (!nearbyPlayers.isEmpty()) {
                next(nearbyPlayers.get(0));
                return;
            }

            if (destroyerDelay > 0) {
                destroyerDelay--;
                return;
            }

            destroyerDelay = DESTROYER_DELAY_TICKS;

            BlockPos pos = nodes.pop();
            BlockPos last = nodes.peek();
            next = last.up();

            double centerX = x + 0.5;
            double centerY = y - 0.5;
            double centerZ = z + 0.5;

            modifier.setBlockState(pos, Blocks.AIR.getDefaultState());
            world.spawnParticles(ParticleTypes.FLAME, centerX, centerY, centerZ, 50, 0.25d, 0.25d, 0.25d, 0.1d);
            world.playSound(null, centerX, centerY, centerZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1f, 0f);

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
        nodes.push(next.down());
        generator.reset();
        destroyerDelay = 0;
        destroyerTimeout = DESTROYER_TIMEOUT_TICKS;

        if (shulkerEntity != null) {
            shulkerEntity.discard();
        }
    }
}
