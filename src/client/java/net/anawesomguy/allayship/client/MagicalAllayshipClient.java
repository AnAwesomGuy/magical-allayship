package net.anawesomguy.allayship.client;

import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.client.entity.FairyRenderer;
import net.anawesomguy.allayship.client.entity.layer.AllaysuitLayer;
import net.anawesomguy.allayship.client.entity.model.AllaysuitModel;
import net.anawesomguy.allayship.client.gui.AllayshipScreen;
import net.anawesomguy.allayship.entity.SuitData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionResult;

public class MagicalAllayshipClient implements ClientModInitializer {
    public static final RenderStateDataKey<SuitData.SuitType> SUIT_TYPE_KEY =
        RenderStateDataKey.create(() -> MagicalAllayship.MOD_ID + ":suit_type");

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(MagicalAllayship.FAIRY, FairyRenderer::new);
        ModelLayerRegistry.registerModelLayer(AllaysuitModel.LAYER_LOCATION, AllaysuitModel::getLayerDef);

        LivingEntityRenderLayerRegistrationCallback.EVENT.register(
            (entityType, entityRenderer, registrationHelper, context) -> {
                if (!(entityRenderer instanceof AvatarRenderer<?> playerRenderer)) {
                    return;
                }

                registrationHelper.register(new AllaysuitLayer(playerRenderer, new AllaysuitModel(
                    context.bakeLayer(AllaysuitModel.LAYER_LOCATION))));
            });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || !player.getItemInHand(hand).is(MagicalAllayship.ALLAYSHIP)) {
                return InteractionResult.PASS;
            }

            Minecraft.getInstance().setScreen(new AllayshipScreen(hand));
            return InteractionResult.SUCCESS;
        });
    }
}