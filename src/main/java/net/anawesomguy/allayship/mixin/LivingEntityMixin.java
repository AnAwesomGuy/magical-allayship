package net.anawesomguy.allayship.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private static final DustParticleOptions DOUBLE_JUMP_DUST = new DustParticleOptions(0xF6F6F6, 1.5F);

    @Shadow
    private int noJumpDelay;

    @Unique
    private boolean didDoubleJump;

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", ordinal = 2))
    private boolean allowDoubleJumpInSuit(boolean original) {
        if (original) {
            this.didDoubleJump = false; // reset double jump
            return true;
        }

        if (!this.didDoubleJump && this.noJumpDelay == 0 && this.hasAttached(MagicalAllayship.SUIT_ATTACHMENT)) {
            this.level().addParticle(DOUBLE_JUMP_DUST,
                                     this.getX() - 0.5 + random.nextDouble(),
                                     this.getY(),
                                     this.getZ() - 0.5 + random.nextDouble(),
                                     0.0, 0.0, 0.0);
            this.level().addParticle(DOUBLE_JUMP_DUST,
                                     this.getX() - 0.5 + random.nextDouble(),
                                     this.getY(),
                                     this.getZ() - 0.5 + random.nextDouble(),
                                     0.0, 0.0, 0.0);
            this.level().addParticle(DOUBLE_JUMP_DUST,
                                     this.getX() - 0.5 + random.nextDouble(),
                                     this.getY(),
                                     this.getZ() - 0.5 + random.nextDouble(),
                                     0.0, 0.0, 0.0);
            this.level().addParticle(DOUBLE_JUMP_DUST,
                                     this.getX() - 0.5 + random.nextDouble(),
                                     this.getY(),
                                     this.getZ() - 0.5 + random.nextDouble(),
                                     0.0, 0.0, 0.0);
            this.level().playPlayerSound(SoundEvents.BREEZE_LAND, SoundSource.PLAYERS, 1.5F, 1F);
            this.didDoubleJump = true;
            return true;
        }
        return false;
    }
}
