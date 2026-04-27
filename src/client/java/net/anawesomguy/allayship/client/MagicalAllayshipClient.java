package net.anawesomguy.allayship.client;

import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.client.entity.FairyRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MagicalAllayshipClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(MagicalAllayship.FAIRY, FairyRenderer::new);
	}
}