package net.anawesomguy.allayship.network;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetTransformationPayload(int playerId, boolean transformed) implements CustomPacketPayload {
    public static final Type<SetTransformationPayload> TYPE = new Type<>(MagicalAllayship.id("set_transformation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetTransformationPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SetTransformationPayload::playerId,
        ByteBufCodecs.BOOL, SetTransformationPayload::transformed,
        SetTransformationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}