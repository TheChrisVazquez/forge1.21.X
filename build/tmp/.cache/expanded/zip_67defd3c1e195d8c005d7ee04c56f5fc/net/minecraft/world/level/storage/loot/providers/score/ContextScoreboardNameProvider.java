package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.ScoreHolder;

public record ContextScoreboardNameProvider(LootContext.EntityTarget target) implements ScoreboardNameProvider {
    public static final MapCodec<ContextScoreboardNameProvider> CODEC = RecordCodecBuilder.mapCodec(
        p_297529_ -> p_297529_.group(LootContext.EntityTarget.CODEC.fieldOf("target").forGetter(ContextScoreboardNameProvider::target))
                .apply(p_297529_, ContextScoreboardNameProvider::new)
    );
    public static final Codec<ContextScoreboardNameProvider> INLINE_CODEC = LootContext.EntityTarget.CODEC
        .xmap(ContextScoreboardNameProvider::new, ContextScoreboardNameProvider::target);

    public static ScoreboardNameProvider forTarget(LootContext.EntityTarget pTarget) {
        return new ContextScoreboardNameProvider(pTarget);
    }

    @Override
    public LootScoreProviderType getType() {
        return ScoreboardNameProviders.CONTEXT;
    }

    @Nullable
    @Override
    public ScoreHolder getScoreHolder(LootContext pContext) {
        return pContext.getParamOrNull(this.target.getParam());
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.target.getParam());
    }
}