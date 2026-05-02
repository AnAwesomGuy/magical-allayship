package net.anawesomguy.allayship.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @ModifyReturnValue(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private boolean onItemAdd(boolean original, int slot, ItemStack itemStack) {
        if (original && itemStack.is(MagicalAllayship.ALLAYSHIP))
            ((AllayshipItem)itemStack.getItem()).onAddToInventory(itemStack, this.player);
        return original;
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void onInventoryLoad(CallbackInfo ci) {
        for (ItemStack itemStack : ((Inventory)(Object)this).getNonEquipmentItems()) {
            if (itemStack.is(MagicalAllayship.ALLAYSHIP)) {
                ((AllayshipItem)itemStack.getItem()).onAddToInventory(itemStack, this.player);
            }
        }
    }
}
