package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RadiationLevelPacket(int level) implements CustomPayload {
    // 0 = none, 1 = far (10 blocks), 2 = close (4 blocks)

    public static final Id<RadiationLevelPacket> ID =
            new Id<>(Identifier.of("tutorialmod", "radiation_level"));

    public static final PacketCodec<RegistryByteBuf, RadiationLevelPacket> CODEC =
            PacketCodecs.INTEGER
                    .xmap(RadiationLevelPacket::new, RadiationLevelPacket::level)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}