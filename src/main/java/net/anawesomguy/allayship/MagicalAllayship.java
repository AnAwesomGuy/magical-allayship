package net.anawesomguy.allayship;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.anawesomguy.allayship.entity.Fairy;
import net.anawesomguy.allayship.entity.SuitData;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.anawesomguy.allayship.network.CallFairyPayload;
import net.anawesomguy.allayship.network.RequestTransformationPayload;
import net.anawesomguy.allayship.network.SetFairyNamePayload;
import net.anawesomguy.allayship.world.FairySavedData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Function;

public class MagicalAllayship implements ModInitializer {
    public static final String MOD_ID = "magical-allayship";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier FAIRY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "fairy");
    public static final EntityType<Fairy> FAIRY;

    static {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, FAIRY_ID);
        FAIRY = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                                  EntityType.Builder.of(Fairy::new, MobCategory.CREATURE)
                                                    .sized(0.35F, 0.6F)
                                                    .eyeHeight(0.36F)
                                                    .ridingOffset(0.04F)
                                                    .clientTrackingRange(8)
                                                    .updateInterval(2)
                                                    .build(key));
    }

    public static final Item HEART_DIAMOND = registerItem("heart_diamond", Item::new, new Item.Properties());
    // fairy data stored in FAIRY_DATA_COMPONENT component
    public static final Item ALLAYSHIP = registerItem("allayship", AllayshipItem::new,
                                                      new Item.Properties().stacksTo(1)
                                                                           .durability(AllayshipItem.MAX_DURABILITY));
    public static final Item FAIRY_SPAWN_EGG = registerItem("fairy_spawn_egg", SpawnEggItem::new,
                                                            new Item.Properties().spawnEgg(FAIRY));

    public static final DataComponentType<Either<UUID, CompoundTag>> FAIRY_DATA_COMPONENT = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("fairy_data"),
        DataComponentType.<Either<UUID, CompoundTag>>builder()
                         .persistent(Codec.either(UUIDUtil.CODEC, CustomData.COMPOUND_TAG_CODEC))
                         .build()
    );
    public static final DataComponentType<Long> ALLAYSHIP_COOLDOWN_END_COMPONENT = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("allayship_cooldown_end"),
        DataComponentType.<Long>builder()
                         .persistent(Codec.LONG)
                         .build()
    );
    public static final DataComponentType<UUID> ALLAYSHIP_ID_COMPONENT = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("allayship_id"),
        DataComponentType.<UUID>builder()
                         .persistent(UUIDUtil.LENIENT_CODEC)
                         .build()
    );

    public static final AttachmentType<SuitData> SUIT_ATTACHMENT = AttachmentRegistry.create(
        id("suit_data"),
        builder -> builder.persistent(SuitData.CODEC).syncWith(SuitData.STREAM_CODEC, AttachmentSyncPredicate.all())
    );

    @Override
    public void onInitialize() {
        // noinspection DataFlowIssue
        FabricDefaultAttributeRegistry.register(FAIRY, Fairy.createAttributes());

        // NETWORKING
        // we should create a networking class if we'll ever have even more networking logic
        PayloadTypeRegistry.serverboundPlay().register(CallFairyPayload.TYPE, CallFairyPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
                           .register(RequestTransformationPayload.TYPE, RequestTransformationPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFairyNamePayload.TYPE, SetFairyNamePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CallFairyPayload.TYPE, (payload, context) -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            AllayshipItem.callFairy(context.player().level(), context.player(), hand);
        });
        ServerPlayNetworking.registerGlobalReceiver(RequestTransformationPayload.TYPE, (payload, context) -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack item = context.player().getItemInHand(hand);
            if (!item.is(ALLAYSHIP))
                return;

            if (!item.has(MagicalAllayship.FAIRY_DATA_COMPONENT)) {
                context.player().sendOverlayMessage(Component.translatable("message.magical-allayship.no-fairy"));
                return;
            }

            if (AllayshipItem.refreshCooldown(item, context.server().overworld().getGameTime())) {
                context.player()
                       .sendOverlayMessage(Component.translatable("message.magical-allayship.allayship-recharging"));
                return;
            }

            if (context.player().hasAttached(SUIT_ATTACHMENT)) {
                context.player().removeAttached(SUIT_ATTACHMENT).removeFrom(context.player());
            } else {
                SuitData suitData = new SuitData(
                    SuitData.SuitType.PINK,
                    context.server().overworld().getGameTime(),
                    AllayshipItem.getOrCreateAllayshipId(item)
                );

                suitData.addTo(context.player());
                context.player().setAttached(SUIT_ATTACHMENT, suitData);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SetFairyNamePayload.TYPE, (payload, context) -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            AllayshipItem.setFairyName(context.player().level(), context.player(), hand, payload.name());
        });

        // store unloaded fairies to level data
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            Entity.RemovalReason removalReason = entity.getRemovalReason();
            if (removalReason == null) {
                return;
            }

            if (entity instanceof Fairy fairy &&
                (removalReason.shouldDestroy() || removalReason.shouldSave()) &&
                !fairy.entityTags().contains(Fairy.IN_ALLAYSHIP_TAG)) {
                FairySavedData.getDataFrom(level)
                              .fairyUuidToData()
                              .put(fairy.getUUID(), AllayshipItem.dataFrom(fairy));
                if (removalReason.shouldSave())
                    fairy.removeAsDiscarded(); // don't save fairy
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> {
            output.accept(FAIRY_SPAWN_EGG);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(HEART_DIAMOND);
        });
        // don't register the allayship item in creative tab menu, you should be getting it through the fairy
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> constructor, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, constructor.apply(properties.setId(key)));
    }

    public static Identifier id(String path) {
        return FAIRY_ID.withPath(path);
    }
}
