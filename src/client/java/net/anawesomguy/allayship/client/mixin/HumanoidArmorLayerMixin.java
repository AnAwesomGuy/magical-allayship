package net.anawesomguy.allayship.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anawesomguy.allayship.client.MagicalAllayshipClient;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void hideArmorWhenSuit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatarState && MagicalAllayshipClient.TRANSFORMED_PLAYERS.contains(avatarState.id)) {
            ci.cancel();
        }
    }
}