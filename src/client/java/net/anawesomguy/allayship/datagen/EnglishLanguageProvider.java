package net.anawesomguy.allayship.datagen;

import net.anawesomguy.allayship.MagicalAllayship;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class EnglishLanguageProvider extends FabricLanguageProvider {
    protected EnglishLanguageProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider lookup, @NonNull TranslationBuilder builder) {
        builder.add(MagicalAllayship.FAIRY, "Fairy");
        builder.add(MagicalAllayship.HEART_DIAMOND, "Heart Diamond");
        builder.add(MagicalAllayship.ALLAYSHIP, "Allayship");
    }
}
