package net.anawesomguy.allayship.client.entity;

import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.Fairy;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FairyRenderer extends MobRenderer<Fairy, AllayRenderState, AllayModel> {
    public static final Identifier TEXTURE = MagicalAllayship.id("textures/entity/fairy.png");

    public FairyRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(AllayRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Fairy entity, AllayRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
    }

    @Override
    public AllayRenderState createRenderState() {
        return new AllayRenderState();
    }

    @Override
    protected int getBlockLightLevel(Fairy entity, BlockPos blockPos) {
        return 15;
    }
}
