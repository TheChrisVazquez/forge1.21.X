package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
    private static final int GOLEM_SCAN_RATE = 200;
    private static final int MEMORY_TIME_TO_LIVE = 599;

    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int pScanRate) {
        super(pScanRate);
    }

    @Override
    protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
        checkForNearbyGolem(pEntity);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(LivingEntity pLivingEntity) {
        Optional<List<LivingEntity>> optional = pLivingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (!optional.isEmpty()) {
            boolean flag = optional.get().stream().anyMatch(p_341383_ -> p_341383_.getType().equals(EntityType.IRON_GOLEM));
            if (flag) {
                golemDetected(pLivingEntity);
            }
        }
    }

    public static void golemDetected(LivingEntity pLivingEntity) {
        pLivingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 599L);
    }
}