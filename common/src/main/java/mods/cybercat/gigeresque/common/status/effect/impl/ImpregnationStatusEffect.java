package mods.cybercat.gigeresque.common.status.effect.impl;

import mod.azure.azurelib.core.object.Color;
import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.client.particle.GigParticles;
import mods.cybercat.gigeresque.common.entity.GigEntities;
import mods.cybercat.gigeresque.common.sound.GigSounds;
import mods.cybercat.gigeresque.common.source.GigDamageSources;
import mods.cybercat.gigeresque.common.status.effect.GigStatusEffects;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;
import mods.cybercat.gigeresque.common.util.DamageSourceUtils;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ImpregnationStatusEffect extends MobEffect {

    public ImpregnationStatusEffect() {
        super(MobEffectCategory.HARMFUL, Color.DARK_GRAY.getColor());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        super.applyEffectTick(livingEntity, amplifier);
        if (GigEntityUtils.isTargetHostable(livingEntity) && this == GigStatusEffects.IMPREGNATION) {
            this.handleStatusEffects(livingEntity, (int) CommonMod.config.impregnationTickTimer, MobEffects.HUNGER,
                    MobEffects.WEAKNESS, MobEffects.DIG_SLOWDOWN);
            if (livingEntity.level().isClientSide()) this.applyParticle(livingEntity);
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @SafeVarargs
    private void handleStatusEffects(@NotNull LivingEntity livingEntity, int ticks, Holder<MobEffect>... statusEffects) {
        for (Holder<MobEffect> effect : statusEffects)
            if (!livingEntity.hasEffect(effect)) livingEntity.addEffect(new MobEffectInstance(effect, ticks, 3, true, true));
    }

    private void applyParticle(@NotNull LivingEntity livingEntity) {
        var random = livingEntity.getRandom();
        if (livingEntity.isAlive() && livingEntity.level().isClientSide) {
            var yOffset = livingEntity.getEyeY() - ((livingEntity.getEyeY() - livingEntity.blockPosition().getY()) / 2.0);
            var customX = livingEntity.getX() + ((random.nextDouble() / 2.0) - 0.5) * (random.nextBoolean() ? -1 : 1);
            var customZ = livingEntity.getZ() + ((random.nextDouble() / 2.0) - 0.5) * (random.nextBoolean() ? -1 : 1);
            for (var i = 0; i < 1 + (int) (livingEntity.getMaxHealth() - livingEntity.getHealth()); i++)
                livingEntity.level().addAlwaysVisibleParticle(GigParticles.BLOOD.get(), customX, yOffset, customZ, 0.0, -0.15,
                        0.0);
        }
    }

    public static void effectRemoval(LivingEntity entity, MobEffectInstance mobEffectInstance) {
        if (Constants.isCreativeSpecPlayer.test(entity)) return;
        if (!GigEntityUtils.isTargetHostable(entity)) return;
        if (entity.level().isClientSide || !(mobEffectInstance.getEffect().value() instanceof ImpregnationStatusEffect)) return;
        if (entity instanceof Mob mob && mob.isNoAi()) return;
        if (entity.hasEffect(GigStatusEffects.TRAUMA)) return;
        LivingEntity burster = createBurster(entity);
        if (burster != null) {
            setBursterProperties(entity, burster);
            entity.level().addFreshEntity(burster);
            entity.level().playSound(entity, entity.blockPosition(), GigSounds.CHESTBURSTING.get(), SoundSource.NEUTRAL, 2.0f,
                    1.0f);
            if (Constants.isNotCreativeSpecPlayer.test(entity)) {
                DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.CHEST), entity.getRandom(), 5, 10);
            }
            entity.hurt(GigDamageSources.of(entity.level(), GigDamageSources.CHESTBURSTING), Integer.MAX_VALUE);
        }
    }

    private static LivingEntity createBurster(LivingEntity entity) {
        var defaultBurster = GigEntities.CHESTBURSTER.get().create(entity.level());
        if (!entity.hasEffect(GigStatusEffects.SPORE) && !entity.hasEffect(GigStatusEffects.DNA)) {
            if (entity.getType().is(GigTags.RUNNER_HOSTS)) {
                var runnerBurster = GigEntities.RUNNERBURSTER.get().create(entity.level());
                if (runnerBurster != null) {
                    runnerBurster.setHostId("runner");
                    return runnerBurster;
                }
            } else if (entity.getType().is(GigTags.AQUATIC_HOSTS)) {
                return GigEntities.AQUATIC_CHESTBURSTER.get().create(entity.level());
            }
        } else if (entity.getType().is(GigTags.NEOHOST) && entity.hasEffect(GigStatusEffects.SPORE)) {
            return GigEntities.NEOBURSTER.get().create(entity.level());
        } else if (entity.getType().is(GigTags.CLASSIC_HOSTS) && entity.hasEffect(GigStatusEffects.DNA)) {
            return GigEntities.SPITTER.get().create(entity.level());
        } else if (entity.getType().is(GigTags.HWG_ENTITIES))
            return GigEntities.HELLMORPH_RUNNER.get().create(entity.level());
        return defaultBurster;
    }

    private static void setBursterProperties(LivingEntity entity, LivingEntity burster) {
        if (entity.hasCustomName()) burster.setCustomName(entity.getCustomName());
        burster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10), burster);
        burster.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
    }
}
