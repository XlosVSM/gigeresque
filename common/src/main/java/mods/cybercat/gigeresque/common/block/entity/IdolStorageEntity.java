package mods.cybercat.gigeresque.common.block.entity;

import mod.azure.azurelib.common.api.common.animatable.GeoBlockEntity;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import mods.cybercat.gigeresque.common.block.GigBlocks;
import mods.cybercat.gigeresque.common.block.storage.SittingIdolBlock;
import mods.cybercat.gigeresque.common.block.storage.StorageProperties;
import mods.cybercat.gigeresque.common.block.storage.StorageStates;
import mods.cybercat.gigeresque.common.entity.GigEntities;

public class IdolStorageEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity {

    public static final EnumProperty<StorageStates> CHEST_STATE = StorageProperties.STORAGE_STATE;

    protected final ContainerOpenersCounter stateManager = new ContainerOpenersCounter() {

        @Override
        protected void onOpen(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state) {
            assert IdolStorageEntity.this.level != null;
            IdolStorageEntity.this.level.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_BREAK,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            );
        }

        @Override
        protected void onClose(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state) {
            assert IdolStorageEntity.this.level != null;
            IdolStorageEntity.this.level.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_BREAK,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            );
        }

        @Override
        protected void openerCountChanged(
            @NotNull Level world,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            int oldViewerCount,
            int newViewerCount
        ) {
            IdolStorageEntity.this.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ChestMenu menu)
                return menu.getContainer() == IdolStorageEntity.this;
            return false;
        }
    };

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public IdolStorageEntity(BlockPos pos, BlockState state) {
        super(GigEntities.ALIEN_STORAGE_BLOCK_ENTITY_3.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IdolStorageEntity blockEntity) {
        if (blockEntity.level != null) {
            if (!blockEntity.level.isClientSide)
                BlockPos.betweenClosed(pos, pos.relative(state.getValue(SittingIdolBlock.FACING), 2).above(2))
                    .forEach(
                        testPos -> {
                            if (
                                !testPos.equals(pos) && !level.getBlockState(testPos)
                                    .is(
                                        GigBlocks.ALIEN_STORAGE_BLOCK_INVIS2.get()
                                    )
                            )
                                level.setBlock(
                                    testPos,
                                    GigBlocks.ALIEN_STORAGE_BLOCK_INVIS2.get().defaultBlockState(),
                                    Block.UPDATE_ALL
                                );
                        }
                    );
            if (!blockEntity.isRemoved())
                blockEntity.stateManager.recheckOpeners(
                    Objects.requireNonNull(blockEntity.getLevel()),
                    blockEntity.getBlockPos(),
                    blockEntity.getBlockState()
                );
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag))
            ContainerHelper.loadAllItems(tag, this.items, registries);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag))
            ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> list) {
        this.items = list;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.gigeresque.alien_storage_block3");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, @NotNull Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_3x3, syncId, inventory, this, 1);
    }

    @Override
    public void startOpen(@NotNull Player player) {
        if (!this.isRemoved() && !player.isSpectator())
            this.stateManager.incrementOpeners(
                player,
                Objects.requireNonNull(this.getLevel()),
                this.getBlockPos(),
                this.getBlockState()
            );
    }

    @Override
    public void stopOpen(@NotNull Player player) {
        if (!this.isRemoved() && !player.isSpectator())
            this.stateManager.decrementOpeners(
                player,
                Objects.requireNonNull(this.getLevel()),
                this.getBlockPos(),
                this.getBlockState()
            );
    }

    public void tick() {
        if (!this.isRemoved())
            this.stateManager.recheckOpeners(
                Objects.requireNonNull(this.getLevel()),
                this.getBlockPos(),
                this.getBlockState()
            );
    }

    protected void onInvOpenOrClose(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        world.blockEvent(pos, state.getBlock(), 1, newViewerCount);
        if (oldViewerCount != newViewerCount)
            if (newViewerCount > 0)
                world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.OPENED));
            else
                world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.CLOSING));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> PlayState.CONTINUE));
    }
}
