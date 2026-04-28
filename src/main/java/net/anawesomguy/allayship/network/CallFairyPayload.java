package net.anawesomguy.allayship.network;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CallFairyPayload(boolean mainHand) implements CustomPacketPayload {
    public static final Type<CallFairyPayload> TYPE = new Type<>(MagicalAllayship.id("call_fairy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CallFairyPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, CallFairyPayload::mainHand,
        CallFairyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}