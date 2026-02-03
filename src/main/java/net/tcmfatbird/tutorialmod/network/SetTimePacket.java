package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetTimePacket(long time) implements CustomPayload {

    public static final Id<SetTimePacket> ID =
            new Id<>(Identifier.of("tutorialmod", "set_time"));

    public static final PacketCodec<RegistryByteBuf, SetTimePacket> CODEC =
            PacketCodecs.VAR_LONG
                    .xmap(SetTimePacket::new, SetTimePacket::time)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}