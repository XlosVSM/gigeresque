package mods.cybercat.gigeresque.common.entity.ai.sensors;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.sblforked.api.core.sensor.ExtendedSensor;
import mod.azure.azurelib.sblforked.api.core.sensor.PredicateSensor;
import mod.azure.azurelib.sblforked.object.SquareRadius;
import mod.azure.azurelib.sblforked.util.BrainUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import mods.cybercat.gigeresque.common.entity.ai.GigMemoryTypes;
import mods.cybercat.gigeresque.common.entity.ai.GigSensors;

public class NearbyRepellentsSensor<E extends LivingEntity> extends PredicateSensor<BlockState, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(GigMemoryTypes.NEARBY_REPELLENT_BLOCKS.get());

    protected SquareRadius radius = new SquareRadius(1, 1);

    public NearbyRepellentsSensor() {
        setPredicate((state, entity) -> !state.isAir());
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return GigSensors.NEARBY_REPELLENT_BLOCKS.get();
    }

    /**
     * Set the radius for the sensor to scan
     *
     * @param radius The coordinate radius, in blocks
     * @return this
     */
    public NearbyRepellentsSensor<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param xz The X/Z coordinate radius, in blocks
     * @param y  The Y coordinate radius, in blocks
     * @return this
     */
    public NearbyRepellentsSensor<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        List<Pair<BlockPos, BlockState>> blocks = new ObjectArrayList<>();

        for (
            BlockPos pos : BlockPos.betweenClosed(
                entity.blockPosition().subtract(this.radius.toVec3i()),
                entity.blockPosition().offset(this.radius.toVec3i())
            )
        ) {
            BlockState state = level.getBlockState(pos);

            if (this.predicate().test(state, entity))
                blocks.add(Pair.of(pos.immutable(), state));
        }

        if (blocks.isEmpty())
            BrainUtils.clearMemory(entity, GigMemoryTypes.NEARBY_REPELLENT_BLOCKS.get());
        else
            BrainUtils.setMemory(entity, GigMemoryTypes.NEARBY_REPELLENT_BLOCKS.get(), blocks);
    }
}
