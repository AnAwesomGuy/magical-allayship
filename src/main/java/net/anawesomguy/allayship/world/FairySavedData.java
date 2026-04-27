package net.anawesomguy.allayship.world;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Map;
import java.util.UUID;

public class FairySavedData extends SavedData {
    public static final Codec<FairySavedData> CODEC = Codec.unboundedMap(UUIDUtil.CODEC, Codec.LONG)
                                                           .xmap(FairySavedData::new,
                                                                 FairySavedData::fairyUuidToDespawnAge);
    @SuppressWarnings("DataFlowIssue")
    public static final SavedDataType<FairySavedData> TYPE = new SavedDataType<>(
        MagicalAllayship.id("fairyDespawnData"), FairySavedData::new, CODEC, null);

    public FairySavedData() {
        this.fairyUuidToDespawnAge = new Object2LongOpenHashMap<>();
    }

    public FairySavedData(Map<UUID, Long> map) {
        this.fairyUuidToDespawnAge = new Object2LongOpenHashMap<>(map);
    }

    public final Object2LongMap<UUID> fairyUuidToDespawnAge;

    public Object2LongMap<UUID> fairyUuidToDespawnAge() {
        return fairyUuidToDespawnAge;
    }
}
