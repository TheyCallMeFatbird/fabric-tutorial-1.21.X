package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TemporalRewindStatePacket(boolean rewinding) implements CustomPayload {

    public static final Id<TemporalRewindStatePacket> ID =
            new Id<>(Identifier.of("tutorialmod", "temporal_rewind_state"));

    public static final PacketCodec<RegistryByteBuf, TemporalRewindStatePacket> CODEC =
            PacketCodecs.BOOL
                    .xmap(TemporalRewindStatePacket::new, TemporalRewindStatePacket::rewinding)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
