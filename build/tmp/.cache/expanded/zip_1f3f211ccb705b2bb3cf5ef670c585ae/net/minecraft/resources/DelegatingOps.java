package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.ListBuilder.Builder;
import com.mojang.serialization.RecordBuilder.MapBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DelegatingOps<T> implements DynamicOps<T> {
    protected final DynamicOps<T> delegate;

    protected DelegatingOps(DynamicOps<T> p_135467_) {
        this.delegate = p_135467_;
    }

    @Override
    public T empty() {
        return this.delegate.empty();
    }

    @Override
    public T emptyMap() {
        return this.delegate.emptyMap();
    }

    @Override
    public T emptyList() {
        return this.delegate.emptyList();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> p_135470_, T p_135471_) {
        return this.delegate.convertTo(p_135470_, p_135471_);
    }

    @Override
    public DataResult<Number> getNumberValue(T p_135518_) {
        return this.delegate.getNumberValue(p_135518_);
    }

    @Override
    public T createNumeric(Number p_135495_) {
        return this.delegate.createNumeric(p_135495_);
    }

    @Override
    public T createByte(byte p_135475_) {
        return this.delegate.createByte(p_135475_);
    }

    @Override
    public T createShort(short p_135497_) {
        return this.delegate.createShort(p_135497_);
    }

    @Override
    public T createInt(int p_135483_) {
        return this.delegate.createInt(p_135483_);
    }

    @Override
    public T createLong(long p_135489_) {
        return this.delegate.createLong(p_135489_);
    }

    @Override
    public T createFloat(float p_135481_) {
        return this.delegate.createFloat(p_135481_);
    }

    @Override
    public T createDouble(double p_135479_) {
        return this.delegate.createDouble(p_135479_);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(T p_135502_) {
        return this.delegate.getBooleanValue(p_135502_);
    }

    @Override
    public T createBoolean(boolean p_135473_) {
        return this.delegate.createBoolean(p_135473_);
    }

    @Override
    public DataResult<String> getStringValue(T p_135522_) {
        return this.delegate.getStringValue(p_135522_);
    }

    @Override
    public T createString(String p_135499_) {
        return this.delegate.createString(p_135499_);
    }

    @Override
    public DataResult<T> mergeToList(T p_135526_, T p_135527_) {
        return this.delegate.mergeToList(p_135526_, p_135527_);
    }

    @Override
    public DataResult<T> mergeToList(T p_135529_, List<T> p_135530_) {
        return this.delegate.mergeToList(p_135529_, p_135530_);
    }

    @Override
    public DataResult<T> mergeToMap(T p_135535_, T p_135536_, T p_135537_) {
        return this.delegate.mergeToMap(p_135535_, p_135536_, p_135537_);
    }

    @Override
    public DataResult<T> mergeToMap(T p_135532_, MapLike<T> p_135533_) {
        return this.delegate.mergeToMap(p_135532_, p_135533_);
    }

    @Override
    public DataResult<T> mergeToMap(T p_335567_, Map<T, T> p_327772_) {
        return this.delegate.mergeToMap(p_335567_, p_327772_);
    }

    @Override
    public DataResult<T> mergeToPrimitive(T p_330125_, T p_335137_) {
        return this.delegate.mergeToPrimitive(p_330125_, p_335137_);
    }

    @Override
    public DataResult<Stream<Pair<T, T>>> getMapValues(T p_135516_) {
        return this.delegate.getMapValues(p_135516_);
    }

    @Override
    public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T p_135514_) {
        return this.delegate.getMapEntries(p_135514_);
    }

    @Override
    public T createMap(Map<T, T> p_336013_) {
        return this.delegate.createMap(p_336013_);
    }

    @Override
    public T createMap(Stream<Pair<T, T>> p_135493_) {
        return this.delegate.createMap(p_135493_);
    }

    @Override
    public DataResult<MapLike<T>> getMap(T p_135512_) {
        return this.delegate.getMap(p_135512_);
    }

    @Override
    public DataResult<Stream<T>> getStream(T p_135520_) {
        return this.delegate.getStream(p_135520_);
    }

    @Override
    public DataResult<Consumer<Consumer<T>>> getList(T p_135508_) {
        return this.delegate.getList(p_135508_);
    }

    @Override
    public T createList(Stream<T> p_135487_) {
        return this.delegate.createList(p_135487_);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(T p_135504_) {
        return this.delegate.getByteBuffer(p_135504_);
    }

    @Override
    public T createByteList(ByteBuffer p_135477_) {
        return this.delegate.createByteList(p_135477_);
    }

    @Override
    public DataResult<IntStream> getIntStream(T p_135506_) {
        return this.delegate.getIntStream(p_135506_);
    }

    @Override
    public T createIntList(IntStream p_135485_) {
        return this.delegate.createIntList(p_135485_);
    }

    @Override
    public DataResult<LongStream> getLongStream(T p_135510_) {
        return this.delegate.getLongStream(p_135510_);
    }

    @Override
    public T createLongList(LongStream p_135491_) {
        return this.delegate.createLongList(p_135491_);
    }

    @Override
    public T remove(T p_135539_, String p_135540_) {
        return this.delegate.remove(p_135539_, p_135540_);
    }

    @Override
    public boolean compressMaps() {
        return this.delegate.compressMaps();
    }

    @Override
    public ListBuilder<T> listBuilder() {
        return new ListBuilder.Builder<>(this);
    }

    @Override
    public RecordBuilder<T> mapBuilder() {
        return new MapBuilder<>(this);
    }

    // Forge start
    java.util.Map<ResourceLocation, Object> ctx = new java.util.HashMap<>();

    @SuppressWarnings("unchecked")
    public <R> R getContext(ResourceLocation rl) {
        var ret = (R)this.ctx.get(rl);
        if (ret == null && this.delegate instanceof DelegatingOps parent) {
            return (R)parent.getContext(rl);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <R> R withContext(ResourceLocation key, Object ctx) {
        this.ctx.put(key, ctx);
        return (R)this;
    }
}
