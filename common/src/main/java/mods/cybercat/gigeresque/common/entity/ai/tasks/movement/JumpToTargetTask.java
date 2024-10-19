package mods.cybercat.gigeresque.common.entity.ai.tasks.movement;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.sblforked.api.core.behaviour.DelayedBehaviour;
import mod.azure.azurelib.sblforked.util.BrainUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mods.cybercat.gigeresque.common.entity.AlienEntity;

public class JumpToTargetTask<E extends AlienEntity> extends DelayedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
        Pair.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED),
        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
    );

    private static final double MAX_LEAP_DISTANCE = 3.0;

    @Nullable
    protected LivingEntity target = null;

    public JumpToTargetTask(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);
        var yDiff = Mth.abs(entity.getBlockY() - target.getBlockY());
        return entity.onGround() && entity.distanceTo(target) > MAX_LEAP_DISTANCE && yDiff > 3
            && entity.getBlockY() != target.getBlockY() && !entity.isCrawling();
    }

    @Override
    protected void doDelayedAction(E entity) {
        if (this.target == null)
            return;
        var vec3d = entity.getDeltaMovement();
        var vec3d2 = new Vec3(target.getX() - entity.getX(), 0.0, target.getZ() - entity.getZ());
        var length = Mth.sqrt((float) vec3d2.length());

        if (vec3d2.lengthSqr() > 1.0E-7)
            vec3d2 = vec3d2.normalize().scale(0.2).add(vec3d.scale(0.2));

        var maxXVel = Mth.clamp(vec3d2.x, -MAX_LEAP_DISTANCE, MAX_LEAP_DISTANCE);
        var maxZVel = Mth.clamp(vec3d2.z, -MAX_LEAP_DISTANCE, MAX_LEAP_DISTANCE);

        if (!entity.horizontalCollision)
            entity.push(maxXVel * length, 1.3, maxZVel * length);
    }
}
