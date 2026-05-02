package net.anawesomguy.allayship.network;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetFairyNamePayload(boolean mainHand, String name) implements CustomPacketPayload {
    public static final Type<SetFairyNamePayload> TYPE = new Type<>(MagicalAllayship.id("set_fairy_name"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFairyNamePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SetFairyNamePayload::mainHand,
        ByteBufCodecs.stringUtf8(20), SetFairyNamePayload::name,
        SetFairyNamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
