package net.anawesomguy.allayship.datagen;

import net.anawesomguy.allayship.MagicalAllayship;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import org.jspecify.annotations.NonNull;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators models) {
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators models) {
        models.generateFlatItem(MagicalAllayship.HEART_DIAMOND, ModelTemplates.FLAT_ITEM);
        models.generateFlatItem(MagicalAllayship.ALLAYSHIP, ModelTemplates.FLAT_ITEM);
        models.generateFlatItem(MagicalAllayship.FAIRY_SPAWN_EGG, ModelTemplates.FLAT_ITEM);
    }
}
