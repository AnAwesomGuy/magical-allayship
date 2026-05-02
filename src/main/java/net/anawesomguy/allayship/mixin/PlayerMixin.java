package net.anawesomguy.allayship.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.SuitData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar {
    @Unique
    private int lastHurtTime;

    @SuppressWarnings("DataFlowIssue")
    protected PlayerMixin() {
        super(null, null);
    }

    @Definition(id = "dmg", local = @Local(type = float.class, ordinal = 0))
    @Expression("@(dmg) != 0.0")
    @ModifyVariable(method = "actuallyHurt", at = @At("MIXINEXTRAS:EXPRESSION"), argsOnly = true, name = "dmg")
    private float absorbDamageWithSuit(float dmg) {
        if (dmg == 0.0F)
            return 0.0F;

        SuitData data = this.getAttached(MagicalAllayship.SUIT_ATTACHMENT);
        if (data == null)
            return dmg;

        float suitDamage = data.damageTaken() + dmg;
        if (suitDamage > this.getMaxHealth()) {
            // noinspection DataFlowIssue (since getAttached is nonnull, removeAttached should return nonnul as well)
            this.removeAttached(MagicalAllayship.SUIT_ATTACHMENT).removeFrom(this); // TODO play sound?
        } else {
            this.setAttached(MagicalAllayship.SUIT_ATTACHMENT, new SuitData(data.type(), data.startTime(), suitDamage));
            this.lastHurtTime = this.tickCount;
        }

        return 0.0F;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onSuitTick(CallbackInfo ci) {
        // noinspection ConstantValue
        if (!((Object)this instanceof ServerPlayer)) // would've injected in ServerPlayer but it would've needed a duck interface
            return;

        SuitData data = this.getAttached(MagicalAllayship.SUIT_ATTACHMENT);
        if (data == null)
            return;

        if (this.tickCount - data.startTime() > 2400) { // break suit after 2 minutes
            this.removeAttached(MagicalAllayship.SUIT_ATTACHMENT).removeFrom(this);
            return;
        }

        if (data.damageTaken() > 0 && this.tickCount % 13 == 0 && this.tickCount - this.lastHurtTime > 320) { // heal every 0.65 seconds after 16 seconds of no damage
            this.setAttached(MagicalAllayship.SUIT_ATTACHMENT, data.heal(this.random.nextBoolean() ? 1 : 2));
        }
    }
}
