package net.anawesomguy.allayship.client.mixin;

import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.client.MagicalAllayshipClient;
import net.anawesomguy.allayship.entity.SuitData;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@NullMarked
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("RETURN"))
    private void addAllaysuitToState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        SuitData suitData = entity.getAttached(MagicalAllayship.SUIT_ATTACHMENT);
        if (suitData != null)
            state.setData(MagicalAllayshipClient.SUIT_TYPE_KEY, suitData.type());
    }
}
