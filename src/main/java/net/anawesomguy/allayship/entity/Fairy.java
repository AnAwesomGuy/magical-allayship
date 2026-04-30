package net.anawesomguy.allayship.entity;

import com.mojang.datafixers.util.Either;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class Fairy extends PathfinderMob {
    public static final String CURRENT_TIME_KEY = "CurrentTime";
    private static final String OWNER_KEY = "Owner";
    private static final String RETURNING_KEY = "Returning";
    public static final String IN_ALLAYSHIP_TAG = "InAllayship";

    private UUID owner;
    private boolean returning;

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

    // p.s. I tried using goals but it ended up being even more effort
    // so this should do I hope
    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || this.owner == null) {
            return;
        }

        Player owner = level.getPlayerByUUID(this.owner);
        if (owner == null) {
            this.owner = null;
            return;
        }

        if (this.returning) {
            Vec3 look = owner.getLookAngle();
            Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(0.45);
            Vec3 target = owner.position().add(side).add(0, 0.9, 0);
            Vec3 movement = target.subtract(this.position());
            // return speed when going back into the allayship
            if (movement.lengthSqr() > 0.18 * 0.18) {
                movement = movement.normalize().scale(0.18);
            }

            this.move(movement);
            if (this.position().distanceToSqr(target) < 0.64) {
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack held = owner.getItemInHand(hand);
                    if (!held.is(MagicalAllayship.ALLAYSHIP)) {
                        continue;
                    }

                    Either<UUID, CompoundTag> data = held.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
                    if (data == null || !data.left().filter(this.getUUID()::equals).isPresent()) {
                        continue;
                    }

                    held.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.right(AllayshipItem.dataFrom(this)));
                    this.addTag(IN_ALLAYSHIP_TAG);
                    this.discard();
                    return;
                }
            }

            return;
        }

        Vec3 look = owner.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(1.5);
        Vec3 target = owner.position().add(side).add(0, 1 + Math.sin(this.tickCount / 10D) * 0.2, 0);
        Vec3 movement = target.subtract(this.position());
        if (movement.lengthSqr() > 0.10 * 0.10) {
            movement = movement.normalize().scale(0.10);
        }

        this.move(movement);
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
        if (this.owner != null)
            output.putString(OWNER_KEY, this.owner.toString());
        output.putBoolean(RETURNING_KEY, this.returning);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = input.getString(OWNER_KEY).map(UUID::fromString).orElse(null);
        this.returning = input.getBooleanOr(RETURNING_KEY, false);
    }

    public void removeAsDiscarded() {
        this.unsetRemoved();
        this.setRemoved(RemovalReason.DISCARDED);
    }

    public void setState(Player player, boolean returning) {
        this.owner = player.getUUID();
        this.returning = returning;
    }

    private void move(Vec3 movement) {
        this.setPos(this.position().add(movement));
        this.setDeltaMovement(movement);
        double horizontal = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        float yRot = (float)(Mth.atan2(movement.z, movement.x) * Mth.RAD_TO_DEG) - 90;
        float xRot = (float)(-(Mth.atan2(movement.y, horizontal) * Mth.RAD_TO_DEG));
        this.setYRot(Mth.approachDegrees(this.getYRot(), yRot, 22));
        this.setXRot(Mth.approachDegrees(this.getXRot(), xRot, 14));
        this.setYHeadRot(this.getYRot());
        this.setYBodyRot(this.getYRot());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                  .add(Attributes.MAX_HEALTH, 10.0)
                  .add(Attributes.FLYING_SPEED, 0.1)
                  .add(Attributes.MOVEMENT_SPEED, 0.1)
                  .add(Attributes.ATTACK_DAMAGE, 2.0);
    }
}
