package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpawnXpSpherePacket(int orbCount, float radius) implements CustomPayload {

    public static final Id<SpawnXpSpherePacket> ID =
            new Id<>(Identifier.of("tutorialmod", "spawn_xp_sphere"));

    public static final PacketCodec<RegistryByteBuf, SpawnXpSpherePacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, SpawnXpSpherePacket::orbCount,
                    PacketCodecs.FLOAT, SpawnXpSpherePacket::radius,
                    SpawnXpSpherePacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
