package mods.cybercat.gigeresque.common.block.petrifiedblocks;

import com.mojang.serialization.MapCodec;
import mods.cybercat.gigeresque.common.block.GigBlocks;
import mods.cybercat.gigeresque.common.block.petrifiedblocks.entity.PetrifiedOjbect5Entity;
import mods.cybercat.gigeresque.common.block.petrifiedblocks.entity.PetrifiedOjbectEntity;
import mods.cybercat.gigeresque.common.block.storage.StorageProperties;
import mods.cybercat.gigeresque.common.block.storage.StorageStates;
import mods.cybercat.gigeresque.common.entity.GigEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PetrifiedObjectBlock5 extends BaseEntityBlock {
    public static final IntegerProperty HATCH = BlockStateProperties.AGE_25;
    public static final EnumProperty<StorageStates> STORAGE_STATE = StorageProperties.STORAGE_STATE;
    public static final MapCodec<PetrifiedObjectBlock5> CODEC = simpleCodec(PetrifiedObjectBlock5::new);

    public PetrifiedObjectBlock5(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(STORAGE_STATE, StorageStates.CLOSED));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack itemStack) {
        if (state.getValue(STORAGE_STATE) == StorageStates.OPENED) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            if (level instanceof ServerLevel serverLevel) {
                var radius = (2 - 1) / 2;
                for (int i = 0; i < 2; i++) {
                    int x = serverLevel.getRandom().nextInt(2) - radius;
                    int z = serverLevel.getRandom().nextInt(2) - radius;
                    var acidEntity = GigEntities.ACID.get().create(serverLevel);
                    assert acidEntity != null;
                    acidEntity.moveTo(pos.offset(x, 0, z), 0, 0);
                    serverLevel.addFreshEntity(acidEntity);
                }
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else {
            dropResources(state, level, pos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    public static void dropResources(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            var d = EntityType.ITEM.getHeight() / 2.0;
            var x = pos.getX() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
            var y = pos.getY() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25) - d;
            var z = pos.getZ() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
            var itemEntity = new ItemEntity(level, x, y, z,
                    GigBlocks.PETRIFIED_OBJECT_5_BLOCK.get().asItem().getDefaultInstance());
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, false);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, STORAGE_STATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return GigEntities.PETRIFIED_OBJECT_5.get().create(pos, state);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(3, 0, 4, 13, 4, 11);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, GigEntities.PETRIFIED_OBJECT_5.get(), PetrifiedOjbect5Entity::tick);
    }
}
