package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_68664_ -> foliagePlacerParts(p_68664_)
                .and(IntProvider.codec(0, 24).fieldOf("crown_height").forGetter(p_161484_ -> p_161484_.crownHeight))
                .apply(p_68664_, MegaPineFoliagePlacer::new)
    );
    private final IntProvider crownHeight;

    public MegaPineFoliagePlacer(IntProvider p_161470_, IntProvider p_161471_, IntProvider p_161472_) {
        super(p_161470_, p_161471_);
        this.crownHeight = p_161472_;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(
        LevelSimulatedReader pLevel,
        FoliagePlacer.FoliageSetter pBlockSetter,
        RandomSource pRandom,
        TreeConfiguration pConfig,
        int pMaxFreeTreeHeight,
        FoliagePlacer.FoliageAttachment pAttachment,
        int pFoliageHeight,
        int pFoliageRadius,
        int pOffset
    ) {
        BlockPos blockpos = pAttachment.pos();
        int i = 0;

        for (int j = blockpos.getY() - pFoliageHeight + pOffset; j <= blockpos.getY() + pOffset; j++) {
            int k = blockpos.getY() - j;
            int l = pFoliageRadius + pAttachment.radiusOffset() + Mth.floor((float)k / (float)pFoliageHeight * 3.5F);
            int i1;
            if (k > 0 && l == i && (j & 1) == 0) {
                i1 = l + 1;
            } else {
                i1 = l;
            }

            this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, new BlockPos(blockpos.getX(), j, blockpos.getZ()), i1, 0, pAttachment.doubleTrunk());
            i = l;
        }
    }

    @Override
    public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
        return this.crownHeight.sample(pRandom);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
        return pLocalX + pLocalZ >= 7 ? true : pLocalX * pLocalX + pLocalZ * pLocalZ > pRange * pRange;
    }
}