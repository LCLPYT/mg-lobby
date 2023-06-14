package work.lclpnet.lobby.decor.jnr;

import it.unimi.dsi.fastutil.ints.IntFloatPair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import work.lclpnet.lobby.util.BlockStateWriter;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Stream;

public class JumpAndRun {

    private final Random random = new Random();
    private final BlockState[] blocksPalette = Stream.of(
            Blocks.BLACK_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA,
            Blocks.GRAY_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA,
            Blocks.LIGHT_BLUE_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.LIME_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.PINK_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA,
            Blocks.RED_TERRACOTTA,
            Blocks.WHITE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA
    ).map(Block::getDefaultState).toArray(BlockState[]::new);

    private final ServerWorld world;
    private final BlockPos start;
    private final Stack<BlockPos> nodes;
    private final PosGenerator generator;
    private final BlockStateWriter writer;
    private BlockPos next;

    public JumpAndRun(ServerWorld world, BlockPos start, BlockStateWriter writer) {
        this.world = world;
        this.start = start;
        this.writer = writer;
        this.nodes = new Stack<>();

        final int maxY = world.getTopY() - start.getY();  // max offset

        @SuppressWarnings("SuspiciousNameCombination")
        var config = new DefaultPosGenerator.Config(25, maxY - 25, List.of(
                // below offset dy, the chance to stay on the same height is reduced by p
                IntFloatPair.of(70, 0.85f),
                IntFloatPair.of(130, 0.75f),
                IntFloatPair.of(maxY, 0.6f)
        ));

        this.generator = new DefaultPosGenerator(world, start.down(), nodes, config);

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
            // goal reached, collapse TODO
            return;
        }

        BlockPos next = generator.generate();
        if (next == null) {
            // generation stuck, collapse TODO
            return;
        }

        nodes.push(next);

        writer.setBlockState(next, randomBlockState());
        world.playSound(null, next, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);

        this.next = next.up();
    }

    private BlockState randomBlockState() {
        return blocksPalette[random.nextInt(blocksPalette.length)];
    }

    private void reset() {
        nodes.clear();
        next = start;
        nodes.push(next.down());
        generator.reset();
    }
}
