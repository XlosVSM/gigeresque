package mods.cybercat.gigeresque.common.status.effect.impl;

import mod.azure.azurelib.core.object.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.block.GigBlocks;
import mods.cybercat.gigeresque.common.source.GigDamageSources;
import mods.cybercat.gigeresque.common.status.effect.GigStatusEffects;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.DamageSourceUtils;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;

public class DNAStatusEffect extends MobEffect {

    private static BlockPos lightBlockPos = null;

    public DNAStatusEffect() {
        super(MobEffectCategory.HARMFUL, Color.DARK_GRAY.getColor());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!entity.getType().is(GigTags.DNAIMMUNE) && this == GigStatusEffects.DNA)
            entity.heal(0);
        return super.applyEffectTick(entity, amplifier);
    }

    public static void effectRemoval(LivingEntity entity, MobEffectInstance mobEffectInstance) {
        var randomPhase = entity.getRandom().nextInt(0, 50);
        if (Constants.isCreeper.test(entity))
            return;
        if (Constants.isCreativeSpecPlayer.test(entity))
            return;
        if (GigEntityUtils.isTargetDNAImmune(entity))
            return;
        if (entity.level().isClientSide || !(mobEffectInstance.getEffect().value() instanceof DNAStatusEffect))
            return;
        if (entity instanceof Mob mob && mob.isNoAi())
            return;
        if (entity.hasEffect(MobEffects.ABSORPTION) && entity.hasEffect(MobEffects.WITHER) && entity.hasEffect(MobEffects.REGENERATION))
            return;
        if (!entity.getType().is(GigTags.DNAIMMUNE)) {
            if (randomPhase > 25) {
                if (
                    (Constants.notPlayer.test(entity) && !(GigEntityUtils.isTargetDNAImmune(
                        entity
                    ))) || Constants.isNotCreativeSpecPlayer.test(entity)
                ) {
                    GigEntityUtils.spawnMutant(entity);
                    entity.hurt(GigDamageSources.of(entity.level(), GigDamageSources.DNA), Integer.MAX_VALUE);
                    if (Constants.isNotCreativeSpecPlayer.test(entity)) {
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.FEET), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.LEGS), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.CHEST), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.HEAD), entity.getRandom(), 10, 50);
                    }
                }
            } else {
                if (
                    (Constants.notPlayer.test(entity) && !(GigEntityUtils.isTargetDNAImmune(
                        entity
                    ))) || Constants.isNotCreativeSpecPlayer.test(entity)
                ) {
                    placeGoo(entity);
                    entity.hurt(GigDamageSources.of(entity.level(), GigDamageSources.DNA), Integer.MAX_VALUE);
                    if (Constants.isNotCreativeSpecPlayer.test(entity)) {
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.FEET), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.LEGS), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.CHEST), entity.getRandom(), 10, 50);
                        DamageSourceUtils.damageArmor(entity.getItemBySlot(EquipmentSlot.HEAD), entity.getRandom(), 10, 50);
                    }
                }
            }
        }
    }

    private static void placeGoo(LivingEntity entity) {
        spawnGoo(entity);
    }

    private static void spawnGoo(LivingEntity entity) {
        if (lightBlockPos == null) {
            lightBlockPos = findFreeSpace(entity.level(), entity.blockPosition());
            if (lightBlockPos == null)
                return;
            var areaEffectCloudEntity = new AreaEffectCloud(
                entity.level(),
                entity.getX(),
                entity.getY(),
                entity.getZ()
            );
            areaEffectCloudEntity.setRadius(2.0F);
            areaEffectCloudEntity.setDuration(300);
            areaEffectCloudEntity.setRadiusPerTick(0);
            areaEffectCloudEntity.addEffect(new MobEffectInstance(GigStatusEffects.DNA, 600, 0));
            entity.level().addFreshEntity(areaEffectCloudEntity);
        } else
            lightBlockPos = null;
    }

    private static BlockPos findFreeSpace(Level world, BlockPos blockPos) {
        if (blockPos == null)
            return null;

        var offsets = new int[2 + 1];
        for (var i = 2; i <= 2; i += 2) {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (var x : offsets)
            for (var y : offsets)
                for (var z : offsets) {
                    var offsetPos = blockPos.offset(x, y, z);
                    var state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(GigBlocks.NEST_RESIN_WEB_CROSS))
                        return offsetPos;
                }

        return null;
    }

}
