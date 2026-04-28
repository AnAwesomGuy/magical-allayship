package net.anawesomguy.allayship.world;

import com.mojang.serialization.Codec;
import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FairySavedData extends SavedData {
    public static final Codec<FairySavedData> CODEC =
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, CustomData.COMPOUND_TAG_CODEC)
             .xmap(FairySavedData::new, fairySavedData -> fairySavedData.fairyUuidToData);
    @SuppressWarnings("DataFlowIssue")
    public static final SavedDataType<FairySavedData> TYPE = new SavedDataType<>(
        MagicalAllayship.id("fairydespawndata"), FairySavedData::new, CODEC, null);

    public static FairySavedData getDataFrom(ServerLevel level) {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
    }

    private final Map<UUID, CompoundTag> fairyUuidToData;

    public FairySavedData() {
        this.fairyUuidToData = new HashMap<>();
    }

    public FairySavedData(Map<UUID, CompoundTag> map) {
        this.fairyUuidToData = new HashMap<>(map);
    }

    public Map<UUID, CompoundTag> fairyUuidToData() {
        setDirty(); // too lazy to write a wrapper for the map so this'll do
        return fairyUuidToData;
    }
}
