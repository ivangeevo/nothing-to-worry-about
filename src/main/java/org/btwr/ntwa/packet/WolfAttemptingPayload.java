package org.btwr.ntwa.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.btwr.ntwa.NTWAMod;

public record WolfAttemptingPayload(int entityId, boolean attempting) implements CustomPayload {

    public static final CustomPayload.Id<WolfAttemptingPayload> ID =
            new CustomPayload.Id<>(Identifier.of(NTWAMod.MOD_ID, "wolf_attempting"));

    public static final PacketCodec<RegistryByteBuf, WolfAttemptingPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, WolfAttemptingPayload::entityId,
                    PacketCodecs.BOOL, WolfAttemptingPayload::attempting,
                    WolfAttemptingPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
