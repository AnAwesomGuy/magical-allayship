package net.anawesomguy.allayship.client.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.SuitData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique
    private static final Identifier SUIT_HEART_FULL = MagicalAllayship.id("hud/suit_heart_full");
    @Unique
    private static final Identifier SUIT_HEART_HALF = MagicalAllayship.id("hud/suit_heart_half");

    @Inject(method = "extractHearts", at = @At("HEAD"))
    private void addSuitHealthLocals(GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci,
                                     @Share("suitHealth") LocalIntRef suitHealthRef) {
        SuitData data = player.getAttached(MagicalAllayship.SUIT_ATTACHMENT);
        suitHealthRef.set(data != null ? Mth.ceil(maxHealth - data.damageTaken()) : 0);
    }

    @Definition(id = "halves", local = @Local(type = int.class, name = "halves"))
    @Definition(id = "currentHealth", local = @Local(type = int.class, name = "currentHealth", argsOnly = true))
    @Expression("halves < currentHealth")
    @ModifyExpressionValue(method = "extractHearts", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean renderSuitHearts(boolean original,
                                     @Local(argsOnly = true) GuiGraphicsExtractor graphics, @Local(name = "xo") int xo, @Local(name = "yo") int yo, @Local(name = "halves") int halves, @Local(name = "currentHealth", argsOnly = true) int currentHealth,
                                     @Share("suitHealth") LocalIntRef suitHealthRef, @Share("halfSuitHeart") LocalBooleanRef halfSuitHeart) {
        halfSuitHeart.set(false);
        if (halves < suitHealthRef.get())
            if ((halves + 1) == suitHealthRef.get()) { // half heart
                if (original) // if we go into the if, draw it later
                    halfSuitHeart.set(true);
                else // otherwise draw it now
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SUIT_HEART_HALF, xo, yo, 9, 9);
                return original;
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SUIT_HEART_FULL, xo, yo, 9, 9);
                return false; // don't draw full heart underneath
            }
        return original;
    }

    @Inject(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V", ordinal = 3, shift = At.Shift.AFTER))
    private void renderSuitHalfHeart(GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci,
                                     @Local(name = "xo") int xo, @Local(name = "yo") int yo, @Share("halfSuitHeart") LocalBooleanRef halfSuitHeart) {
        if (halfSuitHeart.get())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SUIT_HEART_HALF, xo, yo, 9, 9);
    }
}
