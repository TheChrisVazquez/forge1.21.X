package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.mutable.MutableInt;

public class ChunkProtoTickListFix extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of(
        "minecraft:bubble_column", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:seagrass", "minecraft:tall_seagrass"
    );

    public ChunkProtoTickListFix(Schema pOutputSchema) {
        super(pOutputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticfinder = type.findField("Level");
        OpticFinder<?> opticfinder1 = opticfinder.type().findField("Sections");
        OpticFinder<?> opticfinder2 = ((ListType)opticfinder1.type()).getElement().finder();
        OpticFinder<?> opticfinder3 = opticfinder2.type().findField("block_states");
        OpticFinder<?> opticfinder4 = opticfinder2.type().findField("biomes");
        OpticFinder<?> opticfinder5 = opticfinder3.type().findField("palette");
        OpticFinder<?> opticfinder6 = opticfinder.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped(
            "ChunkProtoTickListFix",
            type,
            p_185002_ -> p_185002_.updateTyped(
                    opticfinder,
                    p_185010_ -> {
                        p_185010_ = p_185010_.update(
                            DSL.remainderFinder(),
                            p_185078_ -> DataFixUtils.orElse(
                                    p_185078_.get("LiquidTicks")
                                        .result()
                                        .map(p_185072_ -> p_185078_.set("fluid_ticks", (Dynamic<?>)p_185072_).remove("LiquidTicks")),
                                    p_185078_
                                )
                        );
                        Dynamic<?> dynamic = p_185010_.get(DSL.remainderFinder());
                        MutableInt mutableint = new MutableInt();
                        Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> int2objectmap = new Int2ObjectArrayMap<>();
                        p_185010_.getOptionalTyped(opticfinder1)
                            .ifPresent(
                                p_185018_ -> p_185018_.getAllTyped(opticfinder2)
                                        .forEach(
                                            p_185025_ -> {
                                                Dynamic<?> dynamic3 = p_185025_.get(DSL.remainderFinder());
                                                int k = dynamic3.get("Y").asInt(Integer.MAX_VALUE);
                                                if (k != Integer.MAX_VALUE) {
                                                    if (p_185025_.getOptionalTyped(opticfinder4).isPresent()) {
                                                        mutableint.setValue(Math.min(k, mutableint.getValue()));
                                                    }

                                                    p_185025_.getOptionalTyped(opticfinder3)
                                                        .ifPresent(
                                                            p_185064_ -> int2objectmap.put(
                                                                    k,
                                                                    Suppliers.memoize(
                                                                        () -> {
                                                                            List<? extends Dynamic<?>> list = p_185064_.getOptionalTyped(opticfinder5)
                                                                                .map(
                                                                                    p_326564_ -> p_326564_.write()
                                                                                            .result()
                                                                                            .map(p_185076_ -> p_185076_.asList(Function.identity()))
                                                                                            .orElse(Collections.emptyList())
                                                                                )
                                                                                .orElse(Collections.emptyList());
                                                                            long[] along = p_185064_.get(DSL.remainderFinder())
                                                                                .get("data")
                                                                                .asLongStream()
                                                                                .toArray();
                                                                            return new ChunkProtoTickListFix.PoorMansPalettedContainer(list, along);
                                                                        }
                                                                    )
                                                                )
                                                        );
                                                }
                                            }
                                        )
                            );
                        byte b0 = mutableint.getValue().byteValue();
                        p_185010_ = p_185010_.update(DSL.remainderFinder(), p_184991_ -> p_184991_.update("yPos", p_185067_ -> p_185067_.createByte(b0)));
                        if (!p_185010_.getOptionalTyped(opticfinder6).isPresent() && !dynamic.get("fluid_ticks").result().isPresent()) {
                            int i = dynamic.get("xPos").asInt(0);
                            int j = dynamic.get("zPos").asInt(0);
                            Dynamic<?> dynamic1 = this.makeTickList(dynamic, int2objectmap, b0, i, j, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
                            Dynamic<?> dynamic2 = this.makeTickList(dynamic, int2objectmap, b0, i, j, "ToBeTicked", ChunkProtoTickListFix::getBlock);
                            Optional<? extends Pair<? extends Typed<?>, ?>> optional = opticfinder6.type().readTyped(dynamic2).result();
                            if (optional.isPresent()) {
                                p_185010_ = p_185010_.set(opticfinder6, (Typed<?>)optional.get().getFirst());
                            }

                            return p_185010_.update(
                                DSL.remainderFinder(), p_185035_ -> p_185035_.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", dynamic1)
                            );
                        } else {
                            return p_185010_;
                        }
                    }
                )
        );
    }

    private Dynamic<?> makeTickList(
        Dynamic<?> pData,
        Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> pPalette,
        byte pY,
        int pX,
        int pZ,
        String pName,
        Function<Dynamic<?>, String> pIdGetter
    ) {
        Stream<Dynamic<?>> stream = Stream.empty();
        List<? extends Dynamic<?>> list = pData.get(pName).asList(Function.identity());

        for (int i = 0; i < list.size(); i++) {
            int j = i + pY;
            Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> supplier = pPalette.get(j);
            Stream<? extends Dynamic<?>> stream1 = list.get(i)
                .asStream()
                .mapToInt(p_185074_ -> p_185074_.asShort((short)-1))
                .filter(p_184993_ -> p_184993_ > 0)
                .mapToObj(p_185059_ -> this.createTick(pData, supplier, pX, j, pZ, p_185059_, pIdGetter));
            stream = Stream.concat(stream, stream1);
        }

        return pData.createList(stream);
    }

    private static String getBlock(@Nullable Dynamic<?> p_185032_) {
        return p_185032_ != null ? p_185032_.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> p_185069_) {
        if (p_185069_ == null) {
            return "minecraft:empty";
        } else {
            String s = p_185069_.get("Name").asString("");
            if ("minecraft:water".equals(s)) {
                return p_185069_.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
            } else if ("minecraft:lava".equals(s)) {
                return p_185069_.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
            } else {
                return !ALWAYS_WATERLOGGED.contains(s) && !p_185069_.get("Properties").get("waterlogged").asBoolean(false) ? "minecraft:empty" : "minecraft:water";
            }
        }
    }

    private Dynamic<?> createTick(
        Dynamic<?> pData,
        @Nullable Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> pPalette,
        int pX,
        int pY,
        int pZ,
        int pIndex,
        Function<Dynamic<?>, String> pIdGetter
    ) {
        int i = pIndex & 15;
        int j = pIndex >>> 4 & 15;
        int k = pIndex >>> 8 & 15;
        String s = pIdGetter.apply(pPalette != null ? pPalette.get().get(i, j, k) : null);
        return pData.createMap(
            ImmutableMap.<Dynamic<?>, Dynamic<?>>builder()
                .put(pData.createString("i"), pData.createString(s))
                .put(pData.createString("x"), pData.createInt(pX * 16 + i))
                .put(pData.createString("y"), pData.createInt(pY * 16 + j))
                .put(pData.createString("z"), pData.createInt(pZ * 16 + k))
                .put(pData.createString("t"), pData.createInt(0))
                .put(pData.createString("p"), pData.createInt(0))
                .build()
        );
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> pPalette, long[] pData) {
            this.palette = pPalette;
            this.data = pData;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(pPalette.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        @Nullable
        public Dynamic<?> get(int pX, int pY, int pZ) {
            int i = this.palette.size();
            if (i < 1) {
                return null;
            } else if (i == 1) {
                return (Dynamic<?>)this.palette.get(0);
            } else {
                int j = this.getIndex(pX, pY, pZ);
                int k = j / this.valuesPerLong;
                if (k >= 0 && k < this.data.length) {
                    long l = this.data[k];
                    int i1 = (j - k * this.valuesPerLong) * this.bits;
                    int j1 = (int)(l >> i1 & this.mask);
                    return (Dynamic<?>)(j1 >= 0 && j1 < i ? this.palette.get(j1) : null);
                } else {
                    return null;
                }
            }
        }

        private int getIndex(int pX, int pY, int pZ) {
            return (pY << 4 | pZ) << 4 | pX;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}