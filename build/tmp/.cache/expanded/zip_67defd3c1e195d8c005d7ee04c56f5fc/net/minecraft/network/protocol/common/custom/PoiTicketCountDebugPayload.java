package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PoiTicketCountDebugPayload(BlockPos pos, int freeTicketCount) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PoiTicketCountDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        PoiTicketCountDebugPayload::write, PoiTicketCountDebugPayload::new
    );
    public static final CustomPacketPayload.Type<PoiTicketCountDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_ticket_count");

    private PoiTicketCountDebugPayload(FriendlyByteBuf p_299616_) {
        this(p_299616_.readBlockPos(), p_299616_.readInt());
    }

    private void write(FriendlyByteBuf p_299042_) {
        p_299042_.writeBlockPos(this.pos);
        p_299042_.writeInt(this.freeTicketCount);
    }

    @Override
    public CustomPacketPayload.Type<PoiTicketCountDebugPayload> type() {
        return TYPE;
    }
}