package net.anawesomguy.allayship.network;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestTransformationPayload(boolean mainHand) implements CustomPacketPayload {
    public static final Type<RequestTransformationPayload> TYPE = new Type<>(MagicalAllayship.id("request_transformation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestTransformationPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, RequestTransformationPayload::mainHand,
        RequestTransformationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}