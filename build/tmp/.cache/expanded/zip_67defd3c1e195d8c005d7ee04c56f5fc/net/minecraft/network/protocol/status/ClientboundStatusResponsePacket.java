package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundStatusResponsePacket(ServerStatus status, @org.jetbrains.annotations.Nullable String cachedStatus) implements Packet<ClientStatusPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundStatusResponsePacket> STREAM_CODEC = Packet.codec(
        ClientboundStatusResponsePacket::write, ClientboundStatusResponsePacket::new
    );

    public ClientboundStatusResponsePacket(ServerStatus status) {
        this(status, null);
    }

    private ClientboundStatusResponsePacket(FriendlyByteBuf p_179834_) {
        this(p_179834_.readJsonWithCodec(ServerStatus.CODEC));
    }

    private void write(FriendlyByteBuf p_134899_) {
        if (cachedStatus != null) p_134899_.writeUtf(cachedStatus);
        else
        p_134899_.writeJsonWithCodec(ServerStatus.CODEC, this.status);
    }

    @Override
    public PacketType<ClientboundStatusResponsePacket> type() {
        return StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE;
    }

    public void handle(ClientStatusPacketListener pHandler) {
        pHandler.handleStatusResponse(this);
    }
}
