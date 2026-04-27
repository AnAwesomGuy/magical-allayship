package net.anawesomguy.allayship.item;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class AllayshipItem extends Item {
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
        "hive_pos",
        "Passengers",
        "leash"
    );

    public AllayshipItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.level().isClientSide())
            return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        TypedEntityData<EntityType<?>> entityData = held.remove(DataComponents.ENTITY_DATA);
        if (entityData == null)
            return InteractionResult.PASS;
        CompoundTag entityTag = entityData.copyTagWithoutId();
        IGNORED_TAGS.forEach(entityTag::remove);
        Entity fairy = EntityType.loadEntityRecursive(entityData.type(), entityTag, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (fairy == null)
            return InteractionResult.FAIL;
        fairy.snapTo(player.getEyePosition());
        level.addFreshEntity(fairy);
        level.playSound(player, fairy, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 2F, 1F);
        return InteractionResult.SUCCESS_SERVER;
    }

    public static TypedEntityData<EntityType<?>> dataFrom(Entity entity) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(),
                                                                                            MagicalAllayship.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.save(output);
            IGNORED_TAGS.forEach(output::discard);
            CompoundTag entityTag = output.buildResult();
            return TypedEntityData.of(entity.getType(), entityTag);
        }
    }
}
