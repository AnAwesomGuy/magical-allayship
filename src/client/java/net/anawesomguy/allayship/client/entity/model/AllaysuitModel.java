package net.anawesomguy.allayship.client.entity.model;

import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class AllaysuitModel extends HumanoidModel<AvatarRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MagicalAllayship.id("allaysuit"), "main");

    public AllaysuitModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getLayerDef() {
        MeshDefinition modelData = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F);
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        modelPartData.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                           .texOffs(0, 0)
                           .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.26F))
                           .texOffs(32, 0)
                           .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.46F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition body = modelPartData.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                           .texOffs(40, 16)
                           .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.28F))
                           .texOffs(0, 16)
                           .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        body.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create()
                           .texOffs(32, 48)
                           .mirror()
                           .addBox(-3.0F, -0.25F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.46F))
                           .mirror(false),
            PartPose.offsetAndRotation(3.0F, 11.0F, 0.0F, 0.0F, -0.0175F, -0.2618F)
        );

        body.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create()
                           .texOffs(32, 48)
                           .addBox(-1.0F, -0.25F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.46F)),
            PartPose.offsetAndRotation(-3.0F, 11.0F, 0.0F, 0.0F, 0.0175F, 0.2618F)
        );

        PartDefinition leftwing = body.addOrReplaceChild("leftwing", CubeListBuilder.create(), PartPose.offset(2.0F, 3.0F, 2.0F));

        leftwing.addOrReplaceChild(
            "cube_r3",
            CubeListBuilder.create()
                           .texOffs(0, 46)
                           .addBox(0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8727F, 0.8727F, 0.0F)
        );

        PartDefinition rightwing = body.addOrReplaceChild("rightwing", CubeListBuilder.create(), PartPose.offset(-2.0F, 3.0F, 2.0F));

        rightwing.addOrReplaceChild(
            "cube_r4",
            CubeListBuilder.create()
                           .texOffs(0, 46)
                           .addBox(0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8727F, -0.8727F, 0.0F)
        );

        modelPartData.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create()
                           .texOffs(0, 32)
                           .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F))
                           .texOffs(16, 32)
                           .addBox(-4.0F, -2.0F, -2.0F, 5.0F, 12.0F, 4.0F, new CubeDeformation(0.36F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F)
        );

        modelPartData.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create()
                           .texOffs(0, 32)
                           .mirror()
                           .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F))
                           .mirror(false)
                           .texOffs(16, 32)
                           .mirror()
                           .addBox(-1.0F, -2.0F, -2.0F, 5.0F, 12.0F, 4.0F, new CubeDeformation(0.36F))
                           .mirror(false),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );

        modelPartData.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create()
                           .texOffs(48, 32)
                           .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F))
                           .texOffs(48, 48)
                           .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.46F)),
            PartPose.offset(-2.0F, 12.0F, 0.0F)
        );

        modelPartData.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create()
                           .texOffs(48, 32)
                           .mirror()
                           .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F))
                           .mirror(false)
                           .texOffs(48, 48)
                           .mirror()
                           .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.46F))
                           .mirror(false),
            PartPose.offset(2.0F, 12.0F, 0.0F)
        );

        return LayerDefinition.create(modelData, 64, 64);
    }
}