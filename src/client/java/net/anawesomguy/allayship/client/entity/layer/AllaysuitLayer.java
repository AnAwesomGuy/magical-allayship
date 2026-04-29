package net.anawesomguy.allayship.client.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anawesomguy.allayship.client.MagicalAllayshipClient;
import net.anawesomguy.allayship.client.entity.model.AllaysuitModel;
import net.anawesomguy.allayship.entity.SuitData;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AllaysuitLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final AllaysuitModel model;

    public AllaysuitLayer(AvatarRenderer<?> parent, AllaysuitModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AvatarRenderState state, float yRot, float xRot) {
        SuitData.SuitType suitType = state.getData(MagicalAllayshipClient.SUIT_TYPE_KEY);
        if (suitType == null)
            return;

        this.model.setupAnim(state);
        renderColoredCutoutModel(this.model, suitType.texture, poseStack, submitNodeCollector, packedLight, state, -1, LivingEntityRenderer.getOverlayCoords(state, 0.0F));
    }
}