package mods.cybercat.gigeresque.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import mods.cybercat.gigeresque.client.particle.GigParticles;
import mods.cybercat.gigeresque.common.tags.GigTags;

public class GigRotatedPillarBlock extends RotatedPillarBlock {

    public GigRotatedPillarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (
            (level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.above()).is(GigTags.ALLOW_MIST_BLOCKS)) && pos
                .getY() <= -50
        )
            for (var i = 0; i < 5; i++) {
                var offsetX = random.nextDouble() - 0.5D;
                var offsetY = 1.1D + (random.nextDouble() * 1.3D);
                var offsetZ = random.nextDouble() - 0.5D;

                level.addParticle(
                    GigParticles.MIST.get(),
                    pos.getX() + 0.5D + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5D + offsetZ,
                    0.0D,
                    0.002D,
                    0.0D
                );
            }
    }
}
