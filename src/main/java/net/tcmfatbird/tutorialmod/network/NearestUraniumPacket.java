package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NearestUraniumPacket(int distance) implements CustomPayload {

    public static final Id<NearestUraniumPacket> ID =
            new Id<>(Identifier.of("tutorialmod", "nearest_uranium"));

    public static final PacketCodec<RegistryByteBuf, NearestUraniumPacket> CODEC =
            PacketCodecs.INTEGER
                    .xmap(NearestUraniumPacket::new, NearestUraniumPacket::distance)
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}