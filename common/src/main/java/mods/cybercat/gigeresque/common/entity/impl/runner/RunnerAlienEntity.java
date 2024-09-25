package mods.cybercat.gigeresque.common.entity.impl.runner;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.sblforked.api.SmartBrainOwner;
import mod.azure.azurelib.sblforked.api.core.BrainActivityGroup;
import mod.azure.azurelib.sblforked.api.core.SmartBrainProvider;
import mod.azure.azurelib.sblforked.api.core.behaviour.FirstApplicableBehaviour;
import mod.azure.azurelib.sblforked.api.core.behaviour.OneRandomBehaviour;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.look.LookAtTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.misc.Idle;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.move.MoveToWalkTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.path.SetRandomWalkTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.InvalidateAttackTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.SetPlayerLookTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.SetRandomLookTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.TargetOrRetaliate;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.custom.NearbyBlocksSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.custom.UnreachableTargetSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.vanilla.HurtBySensor;
import mod.azure.azurelib.sblforked.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import mod.azure.azurelib.sblforked.api.core.sensor.vanilla.NearbyPlayersSensor;
import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.AlienEntity;
import mods.cybercat.gigeresque.common.entity.ai.GigNav;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyLightsBlocksSensor;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyRepellentsSensor;
import mods.cybercat.gigeresque.common.entity.ai.tasks.attack.AlienMeleeAttack;
import mods.cybercat.gigeresque.common.entity.ai.tasks.blocks.KillLightsTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.misc.BuildNestTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.movement.FindDarknessTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.movement.FleeFireTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.movement.JumpToTargetTask;
import mods.cybercat.gigeresque.common.entity.helper.AzureVibrationUser;
import mods.cybercat.gigeresque.common.entity.helper.GigAnimationsDefault;
import mods.cybercat.gigeresque.common.entity.helper.GigMeleeAttackSelector;
import mods.cybercat.gigeresque.common.sound.GigSounds;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RunnerAlienEntity extends AlienEntity implements SmartBrainOwner<RunnerAlienEntity> {

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    public RunnerAlienEntity(EntityType<? extends AlienEntity> type, Level world) {
        super(type, world);
        this.vibrationUser = new AzureVibrationUser(this, 1.5f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH,
                CommonMod.config.classicXenoConfigs.classicXenoHealth).add(Attributes.ARMOR, CommonMod.config.runnerConfigs.runnerXenoHealth).add(
                Attributes.ARMOR_TOUGHNESS, CommonMod.config.runnerConfigs.runnerXenoArmor).add(Attributes.KNOCKBACK_RESISTANCE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.3300000041723251).add(Attributes.ATTACK_DAMAGE,
                CommonMod.config.runnerConfigs.runnerXenoAttackDamage).add(Attributes.ATTACK_KNOCKBACK, 1.0);
    }

    @Override
    public int getAcidDiameter() {
        return 3;
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        if (this.wasEyeInWater) return EntityDimensions.scalable(3.0f, 1.0f);
        if (this.isTunnelCrawling() || this.isCrawling()) return EntityDimensions.scalable(0.95f, 0.95f);
        return EntityDimensions.scalable(1.25f, 1.75f);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (target instanceof LivingEntity livingEntity && !this.level().isClientSide && this.getRandom().nextInt(0,
                10) > 7) {
            if (target instanceof Player playerEntity) {
                playerEntity.drop(playerEntity.getInventory().getSelected(), false);
                playerEntity.getInventory().setItem(playerEntity.getInventory().selected, ItemStack.EMPTY);
            }
            if (livingEntity instanceof Mob mobEntity) {
                mobEntity.getMainHandItem();
                this.drop(mobEntity, mobEntity.getMainHandItem());
                mobEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.AIR));
            }
            livingEntity.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
            livingEntity.hurt(damageSources().mobAttack(this),
                    this.getRandom().nextInt(4) > 2 ? CommonMod.config.runnerConfigs.runnerXenoTailAttackDamage : 0.0f);
            this.heal(1.0833f);
            return super.doHurtTarget(target);
        }
        if (target instanceof Creeper creeper) creeper.hurt(damageSources().mobAttack(this), creeper.getMaxHealth());
        this.heal(1.0833f);
        return super.doHurtTarget(target);
    }

    @Override
    public float getGrowthMultiplier() {
        return CommonMod.config.runnerbusterConfigs.runnerAlienGrowthMultiplier;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType != MobSpawnType.NATURAL) setGrowth(getMaxGrowth());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void tick() {
        super.tick();
        GigEntityUtils.breakblocks(this);
        if (this.isPassedOut() && this.getNavigation() != null) ((GigNav)this.getNavigation()).hardStop();
    }

    @Override
    public boolean onClimbable() {
        var blockPos = new BlockPos.MutableBlockPos(this.position().x, this.position().y + 2.0, this.position().z);
        if (this.level().getBlockState(blockPos).blocksMotion()) {
            this.inTwoBlockSpace = true;
        }
        if (!this.level().getBlockState(blockPos).blocksMotion()) {
            this.inTwoBlockSpace = false;
        }
        return this.inTwoBlockSpace;
    }

    @Override
    public boolean isPathFinding() {
        return false;
    }

    @Override
    protected void jumpInLiquid(@NotNull TagKey<Fluid> fluid) {
    }

    @Override
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
        super.customServerAiStep();
    }

    @Override
    public List<ExtendedSensor<RunnerAlienEntity>> getSensors() {
        return ObjectArrayList.of(new NearbyPlayersSensor<>(),
                new NearbyLivingEntitySensor<RunnerAlienEntity>().setPredicate(
                        GigEntityUtils::entityTest),
                new NearbyBlocksSensor<RunnerAlienEntity>().setRadius(7),
                new NearbyRepellentsSensor<RunnerAlienEntity>().setRadius(15).setPredicate(
                        (block, entity) -> block.is(GigTags.ALIEN_REPELLENTS) || block.is(Blocks.LAVA)),
                new NearbyLightsBlocksSensor<RunnerAlienEntity>().setRadius(7).setPredicate(
                        (block, entity) -> block.is(GigTags.DESTRUCTIBLE_LIGHT)), new HurtBySensor<>(),
                new UnreachableTargetSensor<>(), new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<RunnerAlienEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                // Flee Fire
                new FleeFireTask<>(3.5F),
                // Looks at target
                new LookAtTarget<>().stopIf(entity -> this.isPassedOut()).startCondition(
                        entity -> !this.isPassedOut() || !this.isSearching()),
                // Move to target
                new MoveToWalkTarget<>().startCondition(entity -> !this.isPassedOut()).stopIf(
                        entity -> this.isPassedOut()));
    }

    @Override
    public BrainActivityGroup<RunnerAlienEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                // Build Nest
                new BuildNestTask<>(90).startCondition(
                        entity -> !this.isAggressive() || !this.isPassedOut() || !this.isExecuting() || !this.isFleeing() || !this.isCrawling() || !this.isTunnelCrawling()).stopIf(
                        target -> (this.isAggressive() || this.isVehicle() || this.isPassedOut() || this.isFleeing())),
                // Kill Lights
                new KillLightsTask<>().startCondition(
                        entity -> !this.isAggressive() || !this.isPassedOut() || !this.isExecuting() || !this.isFleeing()).stopIf(
                        target -> (this.isAggressive() || this.isVehicle() || this.isPassedOut() || this.isFleeing())),
                // Find Darkness
                new FindDarknessTask<>(),
                // Do first
                new FirstApplicableBehaviour<RunnerAlienEntity>(
                        // Targeting
                        new TargetOrRetaliate<>().stopIf(
                                target -> (this.isAggressive() || this.isVehicle() || this.isFleeing())),
                        // Look at players
                        new SetPlayerLookTarget<>().predicate(
                                target -> target.isAlive() && (!target.isCreative() || !target.isSpectator())).stopIf(
                                entity -> this.isPassedOut() || this.isExecuting()),
                        // Look around randomly
                        new SetRandomLookTarget<>().startCondition(
                                entity -> !this.isPassedOut() || !this.isSearching())).stopIf(
                        entity -> this.isPassedOut() || this.isExecuting()),
                // Random
                new OneRandomBehaviour<>(
                        // Randomly walk around
                        new SetRandomWalkTarget<>().speedModifier(1.2f).startCondition(
                                entity -> !this.isPassedOut() || !this.isExecuting() || !this.isAggressive()).stopIf(
                                entity -> this.isExecuting() || this.isPassedOut() || this.isAggressive() || this.isVehicle()),
                        // Idle
                        new Idle<>().startCondition(entity -> !this.isAggressive()).runFor(
                                entity -> entity.getRandom().nextInt(30, 60))));
    }

    @Override
    public BrainActivityGroup<RunnerAlienEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>().invalidateIf((entity, target) -> GigEntityUtils.removeTarget(target)),
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f).stopIf(entity ->  this.isPassedOut() || this.isVehicle()),
                new JumpToTargetTask<>(20),
                new AlienMeleeAttack<>(5, GigMeleeAttackSelector.RUNNER_ANIM_SELECTOR));
    }

    /*
     * ANIMATIONS
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, Constants.LIVING_CONTROLLER, 5, event -> {
            var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
            if (isDead) return event.setAndContinue(GigAnimationsDefault.DEATH);
            if (event.isMoving() && !(this.isCrawling() || this.isTunnelCrawling()) && !this.isExecuting() && !this.isPassedOut() && !this.swinging && !(this.level().getFluidState(
                    this.blockPosition()).is(Fluids.WATER) && this.level().getFluidState(
                    this.blockPosition()).getAmount() >= 8))
                if (walkAnimation.speedOld >= 0.45F && this.getFirstPassenger() == null)
                    return event.setAndContinue(GigAnimationsDefault.RUN);
                else if (!this.isExecuting() && walkAnimation.speedOld < 0.45F)
                    return event.setAndContinue(GigAnimationsDefault.WALK);
                else if ((this.level().getFluidState(this.blockPosition()).is(
                        Fluids.WATER) && this.level().getFluidState(
                        this.blockPosition()).getAmount() >= 8) && !this.isExecuting() && !this.isVehicle())
                    if (this.isAggressive() && !this.isVehicle())
                        return event.setAndContinue(GigAnimationsDefault.RUSH_SWIM);
                    else return event.setAndContinue(GigAnimationsDefault.SWIM);
            return event.setAndContinue(this.isNoAi() ? GigAnimationsDefault.STATIS_ENTER : (this.level().getFluidState(
                    this.blockPosition()).is(Fluids.WATER) && this.level().getFluidState(
                    this.blockPosition()).getAmount() >= 8) ? GigAnimationsDefault.IDLE_WATER : GigAnimationsDefault.IDLE_LAND);
        }).triggerableAnim("death", GigAnimationsDefault.DEATH) // death
                .triggerableAnim("idle", GigAnimationsDefault.IDLE_LAND) // idle
                .setSoundKeyframeHandler(event -> {
                    if (event.getKeyframeData().getSound().matches("footstepSoundkey") && this.level().isClientSide)
                        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_FOOTSTEP.get(),
                                SoundSource.HOSTILE, 0.5F, 1.0F, true);
                    if (event.getKeyframeData().getSound().matches("idleSoundkey") && this.level().isClientSide)
                        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_AMBIENT.get(),
                                SoundSource.HOSTILE, 1.0F, 1.0F, true);
                })).add(new AnimationController<>(this, Constants.ATTACK_CONTROLLER, 1, event -> {
            if (event.getAnimatable().isPassedOut())
                return event.setAndContinue(RawAnimation.begin().thenLoop("stasis_loop"));
            return PlayState.STOP;
        }).triggerableAnim("alert", GigAnimationsDefault.AMBIENT) // reset hands
                .triggerableAnim("death", GigAnimationsDefault.DEATH) // death
                .triggerableAnim("alert", GigAnimationsDefault.HISS) // reset hands
                .triggerableAnim("passout", GigAnimationsDefault.STATIS_ENTER) // pass out
                .triggerableAnim("passoutloop", GigAnimationsDefault.STATIS_LOOP) // pass out
                .triggerableAnim("wakeup",
                        GigAnimationsDefault.STATIS_LEAVE.then("idle_land", Animation.LoopType.PLAY_ONCE)) // wake up
                .triggerableAnim("swipe", GigAnimationsDefault.LEFT_CLAW) // swipe
                .triggerableAnim("left_claw", GigAnimationsDefault.LEFT_CLAW) // attack
                .triggerableAnim("right_claw", GigAnimationsDefault.RIGHT_CLAW) // attack
                .triggerableAnim("left_tail_basic", GigAnimationsDefault.LEFT_TAIL_BASIC) // attack
                .triggerableAnim("right_tail_basic", GigAnimationsDefault.RIGHT_TAIL_BASIC) // attack
                .setSoundKeyframeHandler(event -> {
                    if (event.getKeyframeData().getSound().matches("clawSoundkey") && this.level().isClientSide)
                        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_CLAW.get(),
                                SoundSource.HOSTILE, 0.25F, 1.0F, true);
                    if (event.getKeyframeData().getSound().matches("tailSoundkey") && this.level().isClientSide)
                        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_TAIL.get(),
                                SoundSource.HOSTILE, 0.25F, 1.0F, true);
                })).add(new AnimationController<>(this, "hissController", 0, event -> {
            var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
            if (this.isHissing() && !this.isVehicle() && !this.isExecuting() && !isDead && !(this.level().getFluidState(
                    this.blockPosition()).is(Fluids.WATER) && this.level().getFluidState(
                    this.blockPosition()).getAmount() >= 8))
                return event.setAndContinue(GigAnimationsDefault.HISS);
            return PlayState.STOP;
        }).setSoundKeyframeHandler(event -> {
            if (event.getKeyframeData().getSound().matches("hissSoundkey") && this.level().isClientSide)
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_HISS.get(),
                        SoundSource.HOSTILE, 1.0F, 1.0F, true);
        }).triggerableAnim("hiss", GigAnimationsDefault.HISS));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
