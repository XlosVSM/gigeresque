package mods.cybercat.gigeresque.common.entity.impl.classic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.AlienEntity;
import mods.cybercat.gigeresque.common.entity.GigEntities;
import mods.cybercat.gigeresque.common.entity.ai.GigNav;
import mods.cybercat.gigeresque.common.entity.ai.sensors.ItemEntitySensor;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyLightsBlocksSensor;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyRepellentsSensor;
import mods.cybercat.gigeresque.common.entity.ai.tasks.blocks.KillCropsTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.blocks.KillLightsTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.misc.AlienPanic;
import mods.cybercat.gigeresque.common.entity.ai.tasks.misc.EatFoodTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.movement.FleeFireTask;
import mods.cybercat.gigeresque.common.entity.helper.AzureVibrationUser;
import mods.cybercat.gigeresque.common.entity.helper.GigAnimationsDefault;
import mods.cybercat.gigeresque.common.entity.helper.Growable;
import mods.cybercat.gigeresque.common.entity.impl.runner.RunnerAlienEntity;
import mods.cybercat.gigeresque.common.sound.GigSounds;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;

public class ChestbursterEntity extends AlienEntity implements Growable, SmartBrainOwner<ChestbursterEntity> {

    public static final EntityDataAccessor<Boolean> BIRTHED = SynchedEntityData.defineId(
        ChestbursterEntity.class,
        EntityDataSerializers.BOOLEAN
    );

    public static final EntityDataAccessor<Boolean> EAT = SynchedEntityData.defineId(
        ChestbursterEntity.class,
        EntityDataSerializers.BOOLEAN
    );

    private static final EntityDataAccessor<Float> BLOOD = SynchedEntityData.defineId(
        ChestbursterEntity.class,
        EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(
        ChestbursterEntity.class,
        EntityDataSerializers.FLOAT
    );

    private final GigNav landNavigation = new GigNav(this, level());

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    public int bloodRendering = 0;

    public int eatingCounter = 0;

    protected String hostId = null;

    public ChestbursterEntity(EntityType<? extends ChestbursterEntity> type, Level world) {
        super(type, world);
        this.vibrationUser = new AzureVibrationUser(this, 0.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.05F, 1.0F, true);
        navigation = landNavigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(
                Attributes.MAX_HEALTH,
                CommonMod.config.chestbursterHealth
            )
            .add(Attributes.ARMOR, 0.0f)
            .add(
                Attributes.ARMOR_TOUGHNESS,
                0.0f
            )
            .add(Attributes.KNOCKBACK_RESISTANCE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3300000041723251)
            .add(
                Attributes.ATTACK_DAMAGE,
                0.0f
            )
            .add(Attributes.ATTACK_KNOCKBACK, 1.0);
    }

    public float getBlood() {
        return entityData.get(BLOOD);
    }

    public void setBlood(float growth) {
        entityData.set(BLOOD, growth);
    }

    @Override
    public int getAcidDiameter() {
        return 1;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public boolean isBirthed() {
        return this.entityData.get(BIRTHED);
    }

    public void setBirthStatus(boolean birth) {
        this.entityData.set(BIRTHED, birth);
    }

    public boolean isEating() {
        return this.entityData.get(EAT);
    }

    public void setEatingStatus(boolean birth) {
        this.entityData.set(EAT, birth);
    }

    @Override
    public float getGrowth() {
        return entityData.get(GROWTH);
    }

    @Override
    public void setGrowth(float growth) {
        entityData.set(GROWTH, growth);
    }

    @Override
    public void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROWTH, 0.0f);
        builder.define(BLOOD, 0.0f);
        builder.define(BIRTHED, false);
        builder.define(EAT, false);
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource source) {
        return GigSounds.HUGGER_HURT.get();
    }

    @Override
    public SoundEvent getDeathSound() {
        return GigSounds.HUGGER_DEATH.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && this.isAlive()) {
            setBlood(bloodRendering++);
            grow(this, 1 * getGrowthMultiplier());
        }
        if (this.isEating())
            eatingCounter++;
        if (eatingCounter >= 20) {
            this.setEatingStatus(false);
            eatingCounter = 0;
        }
        if (this.isBirthed() && this.tickCount > 1200 && this.getGrowth() > 200)
            this.setBirthStatus(false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("growth", getGrowth());
        if (hostId != null)
            nbt.putString("hostId", hostId);
        nbt.putBoolean("is_eating", isEating());
        nbt.putBoolean("is_birthed", isBirthed());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("growth"))
            setGrowth(nbt.getFloat("growth"));
        if (nbt.contains("hostId"))
            hostId = nbt.getString("hostId");
        if (nbt.contains("is_eating"))
            setEatingStatus(nbt.getBoolean("is_eating"));
        if (nbt.contains("is_birthed"))
            setBirthStatus(nbt.getBoolean("is_birthed"));
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
    public List<ExtendedSensor<ChestbursterEntity>> getSensors() {
        return ObjectArrayList.of(
            new NearbyPlayersSensor<>(),
            new NearbyLivingEntitySensor<ChestbursterEntity>().setPredicate(
                GigEntityUtils::entityTest
            ),
            new NearbyBlocksSensor<ChestbursterEntity>().setRadius(7)
                .setPredicate(
                    (block, entity) -> block.is(BlockTags.CROPS)
                ),
            new NearbyRepellentsSensor<ChestbursterEntity>().setRadius(15)
                .setPredicate(
                    (block, entity) -> block.is(GigTags.ALIEN_REPELLENTS) || block.is(Blocks.LAVA)
                ),
            new NearbyLightsBlocksSensor<ChestbursterEntity>().setRadius(7)
                .setPredicate(
                    (block, entity) -> block.is(GigTags.DESTRUCTIBLE_LIGHT)
                ),
            new HurtBySensor<>(),
            new ItemEntitySensor<>(),
            new UnreachableTargetSensor<>(),
            new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<ChestbursterEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
            // Flee Fire
            new FleeFireTask<>(3.5F),
            new AlienPanic(4.0f),
            // Looks at target
            new LookAtTarget<>().stopIf(entity -> this.isPassedOut())
                .startCondition(
                    entity -> !this.isPassedOut() || !this.isSearching()
                ),
            // Move to target
            new MoveToWalkTarget<>().startCondition(entity -> !this.isPassedOut())
                .stopIf(
                    entity -> this.isPassedOut()
                )
        );
    }

    @Override
    public BrainActivityGroup<ChestbursterEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
            // Build Nest
            new EatFoodTask<>(40),
            // Kill Lights
            new KillLightsTask<>(),
            new KillCropsTask<>(),
            // Do first
            new FirstApplicableBehaviour<RunnerAlienEntity>(
                // Targeting
                new TargetOrRetaliate<>().stopIf(
                    target -> (this.isAggressive() || this.isVehicle() || this.isFleeing())
                ),
                // Look at players
                new SetPlayerLookTarget<>().predicate(
                    target -> target.isAlive() && (!target.isCreative() || !target.isSpectator())
                )
                    .stopIf(
                        entity -> this.isPassedOut() || this.isExecuting()
                    ),
                // Look around randomly
                new SetRandomLookTarget<>().startCondition(
                    entity -> !this.isPassedOut() || !this.isSearching()
                )
            ).stopIf(
                entity -> this.isPassedOut() || this.isExecuting()
            ),
            // Random
            new OneRandomBehaviour<>(
                // Randomly walk around
                new SetRandomWalkTarget<>().dontAvoidWater().setRadius(20).speedModifier(1.2f)
            ),
            // Idle
            new Idle<>().startCondition(entity -> !this.isAggressive())
                .runFor(
                    entity -> entity.getRandom().nextInt(30, 60)
                )
        );
    }

    @Override
    public BrainActivityGroup<ChestbursterEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
            new InvalidateAttackTarget<>().invalidateIf((entity, target) -> GigEntityUtils.removeTarget(target))
        );
    }

    /*
     * GROWTH
     */

    @Override
    public float getGrowthMultiplier() {
        return CommonMod.config.bursterConfigs.chestbursterGrowthMultiplier;
    }

    @Override
    public float getMaxGrowth() {
        return Constants.TPD / 2.0f;
    }

    /*
     * TODO: replace runnerburster with rom Cocoon when not runner
     */
    @Override
    public LivingEntity growInto() {
        // LivingEntity entity;
        // if (Objects.equals(hostId, "runner")) entity = GigEntities.RUNNER_ALIEN.get().create(level());
        // else entity = GigEntities.ALIEN_COCOON.get().create(level());
        var entity = GigEntities.RUNNERBURSTER.get().create(level());
        entity.hostId = this.hostId;
        if (hasCustomName())
            entity.setCustomName(this.getCustomName());
        return entity;
    }

    /*
     * ANIMATIONS
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, Constants.LIVING_CONTROLLER, 5, event -> {
            var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
            if (event.isMoving() && !isDead && walkAnimation.speedOld > 0.15F)
                if (walkAnimation.speedOld >= 0.35F)
                    return event.setAndContinue(GigAnimationsDefault.RUSH_SLITHER);
                else
                    return event.setAndContinue(GigAnimationsDefault.SLITHER);
            else if (this.tickCount < 60 && event.getAnimatable().isBirthed())
                return event.setAndContinue(GigAnimationsDefault.BIRTH);
            else
                return event.setAndContinue(GigAnimationsDefault.IDLE);
        }).setSoundKeyframeHandler(event -> {
            if (event.getKeyframeData().getSound().matches("stepSoundkey") && this.level().isClientSide)
                this.level()
                    .playLocalSound(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        GigSounds.BURSTER_CRAWL.get(),
                        SoundSource.HOSTILE,
                        0.25F,
                        1.0F,
                        true
                    );
        }));
        controllers.add(
            new AnimationController<>(
                this,
                Constants.ATTACK_CONTROLLER,
                0,
                event -> PlayState.STOP
            ).triggerableAnim(Constants.EAT, GigAnimationsDefault.CHOMP)
                .triggerableAnim(
                    "death",
                    GigAnimationsDefault.DEATH
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
