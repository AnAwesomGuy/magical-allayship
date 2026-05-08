package net.anawesomguy.allayship.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.minecraft.world.ItemStackWithSlot;
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

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void onInventoryLoad(CallbackInfo ci, @Local ItemStackWithSlot item) {
        if (item.stack().is(MagicalAllayship.ALLAYSHIP))
            ((AllayshipItem)item.stack().getItem()).onAddToInventory(item.stack(), this.player);
    }
}
