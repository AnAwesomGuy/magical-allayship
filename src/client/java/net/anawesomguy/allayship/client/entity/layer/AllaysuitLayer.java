package net.anawesomguy.allayship.client.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.client.MagicalAllayshipClient;
import net.anawesomguy.allayship.client.entity.model.AllaysuitModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.resources.Identifier;

public class AllaysuitLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = MagicalAllayship.id("textures/entity/allaysuit.png");
    private final AllaysuitModel model;

    public AllaysuitLayer(AvatarRenderer<?> parent, AllaysuitModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AvatarRenderState state, float yRot, float xRot) {
        if (!MagicalAllayshipClient.TRANSFORMED_PLAYERS.contains(state.id)) {
            return;
        }

        this.model.setupAnim(state);
        renderColoredCutoutModel(this.model, TEXTURE, poseStack, submitNodeCollector, packedLight, state, -1, LivingEntityRenderer.getOverlayCoords(state, 0.0F));
    }
}