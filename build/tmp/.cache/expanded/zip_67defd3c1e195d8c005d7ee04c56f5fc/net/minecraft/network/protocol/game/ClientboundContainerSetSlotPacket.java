package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetSlotPacket> STREAM_CODEC = Packet.codec(
        ClientboundContainerSetSlotPacket::write, ClientboundContainerSetSlotPacket::new
    );
    public static final int CARRIED_ITEM = -1;
    public static final int PLAYER_INVENTORY = -2;
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int pContainerId, int pStateId, int pSlot, ItemStack pItemStack) {
        this.containerId = pContainerId;
        this.stateId = pStateId;
        this.slot = pSlot;
        this.itemStack = pItemStack.copy();
    }

    private ClientboundContainerSetSlotPacket(RegistryFriendlyByteBuf p_334368_) {
        this.containerId = p_334368_.readByte();
        this.stateId = p_334368_.readVarInt();
        this.slot = p_334368_.readShort();
        this.itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(p_334368_);
    }

    private void write(RegistryFriendlyByteBuf p_330631_) {
        p_330631_.writeByte(this.containerId);
        p_330631_.writeVarInt(this.stateId);
        p_330631_.writeShort(this.slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(p_330631_, this.itemStack);
    }

    @Override
    public PacketType<ClientboundContainerSetSlotPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleContainerSetSlot(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }
}