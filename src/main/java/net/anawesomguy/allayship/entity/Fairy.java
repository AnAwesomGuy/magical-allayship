package net.anawesomguy.allayship.entity;

import com.mojang.datafixers.util.Either;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Fairy extends PathfinderMob {
    public static final String CURRENT_TIME_KEY = "CurrentTime";

    public Fairy(EntityType<? extends Fairy> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && random.nextInt(10) == 0)
            this.level().addParticle(
                ParticleTypes.END_ROD, this.getX(), this.getY() - 0.05, this.getZ(),
                random.nextGaussian() * 0.005, random.nextGaussian() * -0.1, random.nextGaussian() * 0.005);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(MagicalAllayship.HEART_DIAMOND)) {
            held.consume(1, player);
            this.stopRiding();
            this.ejectPassengers();
            this.dropLeash();
            ItemStack stack = MagicalAllayship.ALLAYSHIP.getDefaultInstance();
            stack.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.right(AllayshipItem.dataFrom(this)));
            BehaviorUtils.throwItem(this, stack, position().add(0, 0.5, 0));
            this.level().playSound(player, this, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 2F, 1F);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        MinecraftServer server = this.level().getServer();
        if (server != null)
            output.putLong("CurrentTime", server.overworld().getGameTime());
    }

    public void removeAsDiscarded() {
        this.unsetRemoved();
        this.setRemoved(RemovalReason.DISCARDED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                  .add(Attributes.MAX_HEALTH, 10.0)
                  .add(Attributes.FLYING_SPEED, 0.1)
                  .add(Attributes.MOVEMENT_SPEED, 0.1)
                  .add(Attributes.ATTACK_DAMAGE, 2.0);
    }
}
