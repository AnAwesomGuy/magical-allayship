package net.anawesomguy.allayship.client;

import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.client.entity.FairyRenderer;
import net.anawesomguy.allayship.client.gui.AllayshipScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

public class MagicalAllayshipClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(MagicalAllayship.FAIRY, FairyRenderer::new);
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (!level.isClientSide()) {
				return InteractionResult.PASS;
			}

			if (player.getItemInHand(hand).getItem() != MagicalAllayship.ALLAYSHIP) {
				return InteractionResult.PASS;
			}

			Minecraft.getInstance().setScreen(new AllayshipScreen(hand));
			return InteractionResult.SUCCESS;
		});
	}
}