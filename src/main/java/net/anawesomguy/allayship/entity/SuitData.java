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
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SuitData(SuitType type, long startTime, float damageTaken) {
    public static final Identifier SUIT_SPEED_MODIFIER = MagicalAllayship.id("suit.speed");
    public static final Identifier SUIT_JUMP_MODIFIER = MagicalAllayship.id("suit.jump");

    public static final Codec<SuitData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                                SuitType.CODEC.fieldOf("type").forGetter(SuitData::type),
                                Codec.LONG.fieldOf("startTime").forGetter(SuitData::startTime),
                                Codec.FLOAT.fieldOf("damageTaken").forGetter(SuitData::damageTaken))
                            .apply(instance, SuitData::new));
    public static final StreamCodec<ByteBuf, SuitData> STREAM_CODEC = StreamCodec.composite(
        SuitType.STREAM_CODEC, SuitData::type,
        ByteBufCodecs.LONG, SuitData::startTime,
        ByteBufCodecs.FLOAT, SuitData::damageTaken,
        SuitData::new
    );

    public SuitData(SuitType type, long startTime) {
        this(type, startTime, 0);
    }

    public void addTo(Avatar player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SUIT_SPEED_MODIFIER);
            speedAttribute.addPermanentModifier(
                new AttributeModifier(SUIT_SPEED_MODIFIER, 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        AttributeInstance jumpAttribute = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.removeModifier(SUIT_JUMP_MODIFIER);
            jumpAttribute.addPermanentModifier(
                new AttributeModifier(SUIT_JUMP_MODIFIER, 0.2, AttributeModifier.Operation.ADD_VALUE));
        }

        AttributeInstance safeFallAttribute = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (safeFallAttribute != null) {
            safeFallAttribute.removeModifier(SUIT_JUMP_MODIFIER);
            safeFallAttribute.addPermanentModifier(
                new AttributeModifier(SUIT_JUMP_MODIFIER, 0.8, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public void removeFrom(Avatar player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null)
            speedAttribute.removeModifier(SUIT_SPEED_MODIFIER);

        AttributeInstance jumpAttribute = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttribute != null)
            jumpAttribute.removeModifier(SUIT_JUMP_MODIFIER);

        AttributeInstance safeFallAttribute = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (safeFallAttribute != null) safeFallAttribute.removeModifier(SUIT_JUMP_MODIFIER);
    }

    public SuitData heal(float health) {
        return new SuitData(type, startTime, Math.max(0, damageTaken - health));
    }

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
