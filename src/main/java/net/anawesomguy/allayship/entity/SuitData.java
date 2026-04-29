package net.anawesomguy.allayship.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SuitData(SuitType type, long startTime) {
    public static final Codec<SuitData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                                SuitType.CODEC.fieldOf("type").forGetter(SuitData::type),
                                Codec.LONG.fieldOf("startTime").forGetter(SuitData::startTime))
                            .apply(instance, SuitData::new));
    public static final StreamCodec<ByteBuf, SuitData> STREAM_CODEC = StreamCodec.composite(
        SuitType.STREAM_CODEC, SuitData::type,
        ByteBufCodecs.LONG, SuitData::startTime,
        SuitData::new
    );

    public enum SuitType implements StringRepresentable {
        PINK("pink", "textures/entity/allaysuit.png");

        public static final Codec<SuitType> CODEC = StringRepresentable.fromEnum(SuitType::values);
        public static final StreamCodec<ByteBuf, SuitType> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(SuitType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), SuitType::ordinal);

        public final Identifier texture;
        private final String name;

        SuitType(String name, String texturePath) {
            this.name = name;
            this.texture = MagicalAllayship.id(texturePath);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
