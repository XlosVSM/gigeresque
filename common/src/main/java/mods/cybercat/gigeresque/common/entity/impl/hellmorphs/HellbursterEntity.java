package mods.cybercat.gigeresque.common.entity.impl.hellmorphs;

import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.sblforked.api.core.BrainActivityGroup;
import mod.azure.azurelib.sblforked.api.core.behaviour.FirstApplicableBehaviour;
import mod.azure.azurelib.sblforked.api.core.behaviour.OneRandomBehaviour;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.misc.Idle;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.path.SetRandomWalkTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.SetPlayerLookTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.SetRandomLookTarget;
import mod.azure.azurelib.sblforked.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.level.Level;

import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.GigEntities;
import mods.cybercat.gigeresque.common.entity.ai.tasks.blocks.KillCropsTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.blocks.KillLightsTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.misc.EatFoodTask;
import mods.cybercat.gigeresque.common.entity.helper.AzureVibrationUser;
import mods.cybercat.gigeresque.common.entity.helper.GigAnimationsDefault;
import mods.cybercat.gigeresque.common.entity.helper.Growable;
import mods.cybercat.gigeresque.common.entity.impl.classic.ChestbursterEntity;
import mods.cybercat.gigeresque.common.entity.impl.runner.RunnerbursterEntity;

public class HellbursterEntity extends RunnerbursterEntity implements Growable {

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    public HellbursterEntity(EntityType<? extends HellbursterEntity> type, Level level) {
        super(type, level);
        this.vibrationUser = new AzureVibrationUser(this, 0.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.15F, 1.0F, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(
                Attributes.MAX_HEALTH,
                CommonMod.config.hellbusterConfigs.hellbusterHealth
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
                CommonMod.config.hellbusterConfigs.hellbusterAttackDamage
            )
            .add(Attributes.ATTACK_KNOCKBACK, 0.3);
    }

    /*
     * GROWTH
     */
    @Override
    public float getGrowthMultiplier() {
        return CommonMod.config.hellbusterConfigs.hellbusterGrowthMultiplier;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount < 5) {
            this.triggerAnim(Constants.ATTACK_CONTROLLER, "birth");
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 10), this);
        }
    }

    @Override
    public LivingEntity growInto() {
        LivingEntity alien;
        if (this.getRandom().nextInt(0, 100) >= 51)
            alien = GigEntities.BAPHOMORPH.get().create(level());
        else
            alien = GigEntities.HELLMORPH_RUNNER.get().create(level());

        return alien;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrainActivityGroup<ChestbursterEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
            // Build Nest
            new EatFoodTask<>(40),
            // Kill Lights
            new KillLightsTask<>(),
            new KillCropsTask<>(),
            // Do first
            new FirstApplicableBehaviour<HellbursterEntity>(
                // Targeting
                new TargetOrRetaliate<>().stopIf(
                    target -> (this.isVehicle() || this.isFleeing())
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

    /*
     * ANIMATIONS
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, Constants.LIVING_CONTROLLER, 5, event -> {
            var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
            if (event.isMoving() && !isDead && !this.isInWater())
                if (walkAnimation.speedOld >= 0.35F)
                    return event.setAndContinue(GigAnimationsDefault.RUN);
                else
                    return event.setAndContinue(GigAnimationsDefault.WALK);
            if (event.isMoving() && !isDead && this.isInWater())
                return event.setAndContinue(GigAnimationsDefault.SWIM);
            return event.setAndContinue(this.wasEyeInWater ? GigAnimationsDefault.IDLE_WATER : GigAnimationsDefault.IDLE);
        }));
        controllers.add(
            new AnimationController<>(
                this,
                Constants.ATTACK_CONTROLLER,
                0,
                event -> PlayState.STOP
            ).triggerableAnim("eat", GigAnimationsDefault.CHOMP)
                .triggerableAnim(
                    "birth",
                    GigAnimationsDefault.BIRTH
                )
                .triggerableAnim("death", GigAnimationsDefault.DEATH)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
