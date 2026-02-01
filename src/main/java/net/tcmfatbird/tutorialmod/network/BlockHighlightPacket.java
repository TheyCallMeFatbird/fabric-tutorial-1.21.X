package net.tcmfatbird.tutorialmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public record BlockHighlightPacket(BlockPos pos) implements CustomPayload {

    public static final Id<BlockHighlightPacket> ID =
            new Id<>(Identifier.of("tutorialmod", "block_highlight"));

    public static final PacketCodec<RegistryByteBuf, BlockHighlightPacket> CODEC =
            BlockPos.PACKET_CODEC
                    .collect(PacketCodecs::optional)
                    .xmap(
                            opt -> new BlockHighlightPacket(opt.orElse(null)),
                            packet -> Optional.ofNullable(packet.pos())
                    )
                    .cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}