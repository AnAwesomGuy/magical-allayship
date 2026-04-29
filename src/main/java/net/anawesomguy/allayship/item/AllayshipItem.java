package net.anawesomguy.allayship.item;

import com.mojang.datafixers.util.Either;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.Fairy;
import net.anawesomguy.allayship.world.FairySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class AllayshipItem extends Item {
    public static final float HEALING_SPEED = 20F; // heals 1 health every this many ticks

    private static final List<String> IGNORED_TAGS = List.of(
        "Air",
        "Brain",
        "CanPickUpLoot",
        "DeathTime",
        "fall_distance",
        "FallFlying",
        "Fire",
        "HurtByTimestamp",
        "HurtTime",
        "LeftHanded",
        "Motion",
        "NoGravity",
        "OnGround",
        "PortalCooldown",
        "Pos",
        "Rotation",
        "sleeping_pos",
        "Passengers",
        "leash"
    );

    public AllayshipItem(Properties properties) {
        super(properties);
    }

    public static void callFairy(Level l, Player player, InteractionHand hand) {
        if (!(l instanceof ServerLevel level))
            return;
        ItemStack held = player.getItemInHand(hand);
        Either<UUID, CompoundTag> entityData = held.remove(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (entityData == null)
            return;
        CompoundTag entityTag;
        if (entityData.left().isPresent()) {
            UUID uuid = entityData.left().get();
            CompoundTag data = FairySavedData.getDataFrom(level)
                                             .fairyUuidToData()
                                             .remove(uuid);
            if (data == null) { // fairy still exists in world
                if (level.getEntityInAnyDimension(uuid) instanceof Fairy fairy) {
                    held.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.right(dataFrom(fairy)));
                    fairy.discard();
                    return;
                }
                player.sendOverlayMessage(Component.translatable("message.magical-allayship.fairy-not-found", uuid)
                                                   .withStyle(ChatFormatting.RED));
                return;
            }
            entityTag = data;
        } else
            entityTag = entityData.right().orElseThrow();
        long capturedTime = Optional.ofNullable(entityTag.remove(Fairy.CURRENT_TIME_KEY))
                                    .flatMap(Tag::asLong)
                                    .orElse(0L);
        IGNORED_TAGS.forEach(entityTag::remove);
        Entity entity = EntityType.loadEntityRecursive(MagicalAllayship.FAIRY, entityTag, level,
                                                       EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (!(entity instanceof Fairy fairy))
            return;
        fairy.setHealth(Math.max(fairy.getHealth(), 0F) +
                            ((level.getServer().overworld().getGameTime() - capturedTime) / HEALING_SPEED));
        fairy.snapTo(player.getEyePosition());
        level.addFreshEntity(fairy);
        level.playSound(player, fairy, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 2F, 1F);
    }

    public static CompoundTag dataFrom(Entity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), MagicalAllayship.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.save(output);
            IGNORED_TAGS.forEach(output::discard);
            return output.buildResult();
        }
    }
}
