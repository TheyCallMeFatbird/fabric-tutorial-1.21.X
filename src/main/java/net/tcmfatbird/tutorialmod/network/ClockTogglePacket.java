package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClockTogglePacket(boolean enabled) implements CustomPayload {

    public static final Id<ClockTogglePacket> ID =
            new Id<>(Identifier.of("tutorialmod", "clock_toggle"));

    public static final PacketCodec<RegistryByteBuf, ClockTogglePacket> CODEC =
            PacketCodecs.BOOL
                    .xmap(ClockTogglePacket::new, ClockTogglePacket::enabled)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}