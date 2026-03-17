package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TemporalRewindTogglePacket(boolean rewinding) implements CustomPayload {

    public static final Id<TemporalRewindTogglePacket> ID =
            new Id<>(Identifier.of("tutorialmod", "temporal_rewind_toggle"));

    public static final PacketCodec<RegistryByteBuf, TemporalRewindTogglePacket> CODEC =
            PacketCodecs.BOOL
                    .xmap(TemporalRewindTogglePacket::new, TemporalRewindTogglePacket::rewinding)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
