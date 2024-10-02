package mods.cybercat.gigeresque.common.block;

import mods.cybercat.gigeresque.CommonMod;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.status.effect.GigStatusEffects;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NestResinWebFullBlock extends AbstractNestBlock {
    private int standingTick = 0;

    public NestResinWebFullBlock(Properties settings) {
        super(settings);
    }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (entity.getType().is(GigTags.GIG_ALIENS)) return;
        if (Constants.isCreativeSpecPlayer.test(entity)) return;
        if (entity instanceof LivingEntity livingEntity && GigEntityUtils.isTargetHostable(entity) && !livingEntity.hasEffect(GigStatusEffects.IMPREGNATION)) {
            livingEntity.makeStuckInBlock(state, new Vec3(0.25, 0.05F, 0.25));
            if (!livingEntity.hasEffect(GigStatusEffects.EGGMORPHING))
                livingEntity.addEffect(new MobEffectInstance(GigStatusEffects.EGGMORPHING, (int) CommonMod.config.getEggmorphTickTimer(), 0), entity);
            if (!world.isClientSide())
                standingTick++;
            if (standingTick >= 100) {
                if (!world.getBlockState(pos.below()).is(GigBlocks.NEST_RESIN_WEB_CROSS.get()))
                    livingEntity.setPos(pos.getCenter().x, pos.getY(), pos.getCenter().z);
                if (world.getBlockState(pos.below()).is(GigBlocks.NEST_RESIN_WEB_CROSS.get()))
                    livingEntity.setPos(pos.getCenter().x, pos.below().getY(), pos.getCenter().z);
                livingEntity.makeStuckInBlock(state, new Vec3(0.25, 0.0F, 0.25));
                standingTick = 0;
            }
        } else {
            standingTick = 0;
        }
    }
}
