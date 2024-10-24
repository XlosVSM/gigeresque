package mods.cybercat.gigeresque.common.entity.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.AlienEntity;
import mods.cybercat.gigeresque.common.entity.impl.classic.FacehuggerEntity;
import mods.cybercat.gigeresque.common.entity.impl.mutant.HammerpedeEntity;
import mods.cybercat.gigeresque.common.entity.impl.mutant.PopperEntity;
import mods.cybercat.gigeresque.common.tags.GigTags;

public class AzureVibrationUser implements VibrationSystem.User {

    private final AlienEntity mob;

    private final float moveSpeed;

    private final PositionSource positionSource;

    public AzureVibrationUser(AlienEntity entity, float speed) {
        this.positionSource = new EntityPositionSource(entity, entity.getEyeHeight());
        this.mob = entity;
        this.moveSpeed = speed;
    }

    @Override
    public int getListenerRadius() {
        return CommonMod.config.xenoMaxSoundRange;
    }

    @Override
    public @NotNull PositionSource getPositionSource() {
        return this.positionSource;
    }

    @Override
    public @NotNull TagKey<GameEvent> getListenableEvents() {
        return GigTags.ALIEN_CAN_LISTEN;
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    @Override
    public boolean isValidVibration(Holder<GameEvent> gameEvent, @NotNull Context context) {
        if (!gameEvent.is(this.getListenableEvents()))
            return false;

        var entity = context.sourceEntity();
        if (entity != null) {
            if (this.mob.isAggressive())
                return false;
            if (entity.isSpectator())
                return false;
            if (entity.dampensVibrations())
                return false;
            if (
                this.mob.level()
                    .getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(3))
                    .stream()
                    .anyMatch(target -> !target.getType().is(GigTags.GIG_ALIENS))
            )
                return true;
            if (
                this.mob.level()
                    .getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(this.getListenerRadius()))
                    .stream()
                    .anyMatch(
                        Entity::isSteppingCarefully
                    )
            )
                return false;
        }
        if (context.affectedState() != null)
            return !context.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
        return true;
    }

    @Override
    public boolean canReceiveVibration(
        @NotNull ServerLevel serverLevel,
        @NotNull BlockPos blockPos,
        @NotNull Holder<GameEvent> gameEvent,
        @NotNull Context context
    ) {
        if (
            mob.isNoAi() || mob.isDeadOrDying() || !mob.level()
                .getWorldBorder()
                .isWithinBounds(
                    blockPos
                ) || mob.isRemoved()
        )
            return false;
        var entity = context.sourceEntity();
        return !(entity instanceof LivingEntity) || mob.canTargetEntity(entity);
    }

    @Override
    public void onReceiveVibration(
        @NotNull ServerLevel serverLevel,
        @NotNull BlockPos blockPos,
        @NotNull Holder<GameEvent> gameEvent,
        @Nullable Entity entity,
        @Nullable Entity entity2,
        float f
    ) {
        if (this.mob.isDeadOrDying())
            return;
        if (this.mob.isVehicle())
            return;
        this.doVibrationAction(blockPos, entity2);
    }

    @SuppressWarnings("deprecation")
    private void doVibrationAction(@NotNull BlockPos blockPos, @Nullable Entity entity2) {
        if (!this.mob.isCrawling() && !this.mob.isTunnelCrawling()) {
            this.mob.wakeupCounter++;
            if (this.mob.isPassedOut() && this.mob.wakeupCounter == 1)
                this.mob.triggerAnim(Constants.ATTACK_CONTROLLER, "wakeup");
            if (this.mob.wakeupCounter == 2) {
                if (this.mob.level().getBlockState(this.mob.blockPosition().below()).isSolid())
                    this.mob.setPassedOutStatus(false);
                this.mob.triggerAnim(Constants.ATTACK_CONTROLLER, "alert");
            }
            if (this.mob.wakeupCounter >= 3) {
                this.mob.triggerAnim(Constants.ATTACK_CONTROLLER, "run");
                this.mob.setPassedOutStatus(false);
                this.mob.setAggressive(true);
                this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.moveSpeed);
                this.mob.wakeupCounter = 0;
            }
        }

        if (this.mob.isCrawling() || this.mob.isTunnelCrawling()) {
            this.mob.setPassedOutStatus(false);
            this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.moveSpeed);
        }

        if (
            this.mob instanceof PopperEntity || this.mob instanceof HammerpedeEntity || this.mob instanceof FacehuggerEntity
                && !(entity2 instanceof IronGolem)
        )
            mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.moveSpeed);
    }

}
