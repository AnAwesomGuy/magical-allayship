package net.anawesomguy.allayship.item;

import com.mojang.datafixers.util.Either;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.Fairy;
import net.anawesomguy.allayship.world.FairySavedData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@NullMarked
public class AllayshipItem extends Item {
    public static final int MAX_DURABILITY = 180;
    public static final int ACTIVE_DMG_INTERVAL = 20;
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
        "leash",
        Fairy.OWNER_KEY,
        Fairy.RETURNING_KEY
    );

    public AllayshipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        refreshCooldown(stack, level.getGameTime());
    }

    public static void callFairy(ServerLevel level, ServerPlayer player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        Either<UUID, CompoundTag> entityData = held.remove(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (entityData == null)
            return;
        CompoundTag entityTag;
        if (entityData.left().isPresent()) {
            UUID uuid = entityData.left().get();
            if (level.getEntityInAnyDimension(uuid) instanceof Fairy fairy) {
                FairySavedData.getDataFrom(level)
                              .fairyUuidToData()
                              .remove(uuid);
                held.set(MagicalAllayship.FAIRY_DATA_COMPONENT, entityData);
                fairy.setState(player, true);
                return;
            }

            CompoundTag data = FairySavedData.getDataFrom(level)
                                            .fairyUuidToData()
                                            .remove(uuid);
            if (data == null) {
                player.sendOverlayMessage(Component.translatable("message.magical-allayship.fairy-not-found", uuid)
                                                   .withStyle(ChatFormatting.RED));
                return;
            }
            entityTag = data;
        } else
            entityTag = entityData.right().orElseThrow();

        long capturedTime = 0L;
        Tag oldTimeTag = entityTag.remove(Fairy.CURRENT_TIME_KEY);
        if (oldTimeTag != null)
            capturedTime = oldTimeTag.asLong().orElse(0L);

        // summon fairy
        IGNORED_TAGS.forEach(entityTag::remove);
        Entity entity = EntityType.loadEntityRecursive(MagicalAllayship.FAIRY, entityTag, level,
                                                       EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (!(entity instanceof Fairy fairy))
            return;
        fairy.setState(player, false);
        fairy.setHealth(Math.max(fairy.getHealth(), 0F) +
                            ((level.getServer().overworld().getGameTime() - capturedTime) / HEALING_SPEED));
        fairy.snapTo(player.getEyePosition());
        level.addFreshEntity(fairy);
        held.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.left(fairy.getUUID()));
        level.playSound(player, fairy, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 2F, 1F);
    }

    public static void setFairyName(ServerLevel level, ServerPlayer player, InteractionHand hand, String name) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(MagicalAllayship.ALLAYSHIP)) {
            return;
        }

        Either<UUID, CompoundTag> entityData = held.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (entityData == null) {
            return;
        }

        String trimmedName = name.trim();
        CompoundTag data;
        if (entityData.left().isPresent()) {
            UUID uuid = entityData.left().get();
            if (level.getEntityInAnyDimension(uuid) instanceof Fairy fairy) {
                fairy.setCustomName(trimmedName.isBlank() ? null : Component.literal(trimmedName));
                fairy.setCustomNameVisible(!trimmedName.isBlank());
            }

            data = FairySavedData.getDataFrom(level).fairyUuidToData().get(uuid);
            if (data != null) {
                setName(data, trimmedName);
            }

            return;
        }

        data = entityData.right().orElseThrow();
        setName(data, trimmedName);
        held.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.right(data));
    }

    public static CompoundTag dataFrom(Entity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), MagicalAllayship.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.save(output);
            IGNORED_TAGS.forEach(output::discard);
            return output.buildResult();
        }
    }

    public static UUID getOrCreateAllayshipId(ItemStack stack) {
        UUID allayshipId = stack.get(MagicalAllayship.ALLAYSHIP_ID_COMPONENT);
        if (allayshipId == null) {
            allayshipId = UUID.randomUUID();
            stack.set(MagicalAllayship.ALLAYSHIP_ID_COMPONENT, allayshipId);
        }

        return allayshipId;
    }

    public static boolean damageAllayship(ServerPlayer player, UUID allayshipId) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(MagicalAllayship.ALLAYSHIP) || !allayshipId.equals(getOrCreateAllayshipId(stack))) {
            stack = player.getOffhandItem();
        }

        if (!stack.is(MagicalAllayship.ALLAYSHIP) || !allayshipId.equals(getOrCreateAllayshipId(stack))) {
            for (ItemStack inventoryStack : player.getInventory().getNonEquipmentItems()) {
                if (inventoryStack.is(MagicalAllayship.ALLAYSHIP) &&
                    allayshipId.equals(getOrCreateAllayshipId(inventoryStack))) {
                    stack = inventoryStack;
                    break;
                }
            }
        }

        if (stack.isEmpty()) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        if (refreshCooldown(stack, gameTime)) {
            return false;
        }

        int maxDamage = stack.getMaxDamage();
        if (stack.getDamageValue() >= maxDamage - 2) {
            stack.setDamageValue(maxDamage - 1);
            // wait for 10 mins
            stack.set(MagicalAllayship.ALLAYSHIP_COOLDOWN_END_COMPONENT, gameTime + 12000L);
            return false;
        }

        stack.setDamageValue(stack.getDamageValue() + 1);
        return true;
    }

    public static boolean refreshCooldown(ItemStack stack, long gameTime) {
        Long cooldownEnd = stack.get(MagicalAllayship.ALLAYSHIP_COOLDOWN_END_COMPONENT);
        if (cooldownEnd == null) {
            return false;
        }

        if (cooldownEnd > gameTime) {
            return true;
        }

        stack.remove(MagicalAllayship.ALLAYSHIP_COOLDOWN_END_COMPONENT);
        stack.setDamageValue(0);
        return false;
    }

    // called in InventoryMixin
    public void onAddToInventory(ItemStack stack, Player player) {
        getOrCreateAllayshipId(stack);
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        Either<UUID, CompoundTag> entityData = stack.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (entityData == null || entityData.left().isEmpty()) {
            return;
        }

        UUID uuid = entityData.left().get();
        if (player.level().getEntityInAnyDimension(uuid) instanceof Fairy fairy) {
            fairy.setState(player, false);
            FairySavedData.getDataFrom(level).fairyUuidToData().remove(uuid);
            return;
        }

        CompoundTag data = FairySavedData.getDataFrom(level).fairyUuidToData().remove(uuid);
        if (data == null) {
            return;
        }

        Entity entity = EntityType.loadEntityRecursive(MagicalAllayship.FAIRY, data, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (!(entity instanceof Fairy fairy)) {
            return;
        }

        fairy.setState(player, false);
        fairy.snapTo(player.getEyePosition());
        level.addFreshEntity(fairy);
        stack.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.left(fairy.getUUID()));
    }

    private static void setName(CompoundTag data, String name) {
        if (name.isBlank()) {
            data.remove("CustomName");
            data.remove("CustomNameVisible");
            return;
        }

        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, Component.literal(name))
                                    .result()
                                    .ifPresent(tag -> data.put("CustomName", tag));
        data.putBoolean("CustomNameVisible", true);
    }
}
