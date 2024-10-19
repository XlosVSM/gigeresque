package mods.cybercat.gigeresque.common.entity.ai.tasks.misc;

import com.mojang.datafixers.util.Pair;
import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.sblforked.api.core.behaviour.DelayedBehaviour;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;

import mods.cybercat.gigeresque.interfacing.AbstractAlien;

public class SearchTask<E extends PathfinderMob & AbstractAlien & GeoEntity> extends DelayedBehaviour<E> {

    public SearchTask(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return entity.getDeltaMovement().horizontalDistance() == 0 && !entity.isInWater() && !entity.isAggressive() && !entity.isVehicle()
            && !entity.isHissing() && entity.isAlive() && !entity.isPassedOut() && !entity.isTunnelCrawling() && !entity.isCrawling();
    }

    @Override
    protected void stop(E entity) {
        entity.setIsSearching(false);
        super.stop(entity);
    }

    @Override
    protected void doDelayedAction(E entity) {
        entity.setIsSearching(true);
    }
}
