package net.anawesomguy.allayship.entity;

import com.mojang.datafixers.util.Either;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.item.AllayshipItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class Fairy extends PathfinderMob {
    public static final String CURRENT_TIME_KEY = "CurrentTime";
    public static final String OWNER_KEY = "Owner";
    public static final String RETURNING_KEY = "Returning";
    public static final String IN_ALLAYSHIP_TAG = "InAllayship";

    @Nullable
    private UUID owner;
    @Nullable
    private Vec3 target;
    @Nullable
    private BlockPos interest;
    private double angle;
    private int moveType;
    private int moveTicks;
    private int waitTicks;
    private int lookTicks;
    private boolean returning;

    public Fairy(EntityType<? extends Fairy> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
        if (this.level().isClientSide() && random.nextInt(10) == 0)
            this.level().addParticle(
                ParticleTypes.END_ROD, this.getX(), this.getY() - 0.05, this.getZ(),
                random.nextGaussian() * 0.005, random.nextGaussian() * -0.1, random.nextGaussian() * 0.005);
    }

    // MOB AI
    // this is entirely custom; I find the way vanilla does things a bit underwhelming for
    // our needs
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.owner == null) {
            if (!this.hasHome()) {
                // keep ownerless fairies around where they were spawned
                this.setHomeTo(this.blockPosition(), 3);
            }

            // once they reach something interesting, stay nearby for a bit and wander around it
            if (this.lookTicks > 0 && this.interest != null) {
                this.lookTicks--;
                if (this.moveTicks-- <= 0) {
                    this.moveType = this.random.nextInt(4);
                    this.moveTicks = 18 + this.random.nextInt(45);
                }

                if (this.moveType == 1) {
                    this.angle += 0.012 + (this.getId() % 4) * 0.003;
                } else if (this.moveType == 2) {
                    this.angle -= 0.006 + (this.getId() % 2) * 0.002;
                }

                this.target = this.hoverAround(this.interest);

                Vec3 movement = this.target.subtract(this.position());
                // tweak inspectig speed here
                if (movement.lengthSqr() > 0.009 * 0.009) {
                    movement = movement.normalize().scale(0.009);
                }

                this.move(movement, false);
                this.lookAt(this.lookAround(this.interest));
                if (this.lookTicks == 0) {
                    this.target = null;
                    this.interest = null;
                    // wait between targets
                    this.waitTicks = 5 + this.random.nextInt(20);
                }

                return;
            }

            double arriveDistance = this.interest == null ? 1.2 : 0.45;
            if (this.target != null && this.position().distanceToSqr(this.target) < arriveDistance * arriveDistance) {
                if (this.interest != null) {
                    this.lookTicks = 35 + this.random.nextInt(55);
                    this.move(Vec3.ZERO, true);
                    this.lookAt(Vec3.atCenterOf(this.interest));
                    return;
                }

                this.target = null;
            }

            if (this.waitTicks-- <= 0) {
                this.target = null;
                this.interest = null;
            }

            if (this.target == null) {
                BlockPos block = this.findInterest();
                this.interest = block;
                this.angle = this.random.nextDouble() * Mth.TWO_PI;
                this.moveType = this.random.nextInt(4);
                this.moveTicks = 18 + this.random.nextInt(45);
                this.target = block == null ? this.wanderTarget() : this.hoverAround(block);
                this.waitTicks = 160 + this.random.nextInt(100);
            }

            Vec3 movement = this.target.subtract(this.position());
            // speed while the alling is heading somewhere
            if (movement.lengthSqr() > 0.035 * 0.035) {
                movement = movement.normalize().scale(0.035);
            }

            this.move(movement, this.interest == null);
            if (this.interest != null) {
                this.lookAt(Vec3.atCenterOf(this.interest));
            }

            return;
        }

        Player owner = level.getPlayerByUUID(this.owner);
        if (owner == null) {
            return;
        }

        if (this.returning) {
            Vec3 look = owner.getLookAngle();
            Vec3 side = new Vec3(-look.z, 0, look.x).normalize().scale(0.45);
            Vec3 target = owner.getEyePosition().add(side);
            Vec3 movement = target.subtract(this.position());
            // return speed when going back into the allayship
            if (movement.lengthSqr() > (0.18 * 0.18)) {
                movement = movement.normalize().scale(0.18);
            }

            this.move(movement, true);
            if (this.position().distanceToSqr(target) < 0.64) {
                for (ItemStack stack : owner.getInventory().getNonEquipmentItems()) {
                    if (!stack.is(MagicalAllayship.ALLAYSHIP)) {
                        continue;
                    }

                    Either<UUID, CompoundTag> data = stack.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
                    if (data == null || data.left().filter(this.getUUID()::equals).isEmpty()) {
                        continue;
                    }

                    stack.set(MagicalAllayship.FAIRY_DATA_COMPONENT, Either.right(AllayshipItem.dataFrom(this)));
                    this.addTag(IN_ALLAYSHIP_TAG);
                    this.discard();
                    return;
                }
            }

            return;
        }

        Vec3 look = owner.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        Vec3 currentSide = this.position().subtract(owner.position()).multiply(1, 0, 1);
        if (currentSide.lengthSqr() > 0.2 * 0.2 && this.position().distanceToSqr(owner.position()) < 3 * 3) {
            side = currentSide.normalize().lerp(side.normalize(), 0.08);
        }

        side = side.normalize().scale(1.5);
        Vec3 target = owner.position().add(side).add(0, 1 + Math.sin(this.tickCount / 10D) * 0.2, 0);
        Vec3 movement = target.subtract(this.position());
        if (movement.lengthSqr() > (0.10 * 0.10)) {
            movement = movement.normalize().scale(0.10);
        }

        this.move(movement, true);
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
        this.clearHome();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    private void move(Vec3 movement, boolean rotate) {
        this.setDeltaMovement(movement);
        super.move(MoverType.SELF, movement);
        if (!rotate || movement.lengthSqr() < 0.001 * 0.001) {
            return;
        }

        double horizontal = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        float yRot = (float)(Mth.atan2(movement.z, movement.x) * Mth.RAD_TO_DEG) - 90;
        float xRot = (float)(-(Mth.atan2(movement.y, horizontal) * Mth.RAD_TO_DEG));
        this.setYRot(Mth.approachDegrees(this.getYRot(), yRot, 22));
        this.setXRot(Mth.approachDegrees(this.getXRot(), xRot, 14));
        this.setYHeadRot(this.getYRot());
        this.setYBodyRot(this.getYRot());
    }

    private void lookAt(Vec3 target) {
        this.getLookControl().setLookAt(target.x, target.y, target.z);
        Vec3 direction = target.subtract(this.getEyePosition());
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        this.setYRot(Mth.approachDegrees(this.getYRot(), (float)(Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90, 10));
        this.setXRot(Mth.approachDegrees(this.getXRot(), (float)(-(Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG)), 8));
        this.setYHeadRot(this.getYRot());
        this.setYBodyRot(this.getYRot());
    }

    private Vec3 hoverAround(BlockPos interest) {
        double bob = Math.sin((this.tickCount + this.getId()) / 18D) * 0.08;
        double radius = 0.5;
        if (this.moveType == 0) {
            radius = 0.38;
        } else if (this.moveType == 2) {
            radius = 0.62;
        } else if (this.moveType == 3) {
            radius = 0.48 + Math.sin((this.tickCount + this.getId()) / 35D) * 0.12;
        }

        return Vec3.atBottomCenterOf(interest).add(Math.cos(this.angle) * radius, 0.65 + bob, Math.sin(this.angle) * radius);
    }

    private Vec3 lookAround(BlockPos interest) {
        double angle = this.angle * 0.35 + Math.sin((this.tickCount + this.getId()) / 31D) * 0.45;
        return Vec3.atCenterOf(interest).add(Math.cos(angle) * 0.18, Math.sin((this.tickCount + this.getId()) / 27D) * 0.12, Math.sin(angle) * 0.18);
    }

    private @Nullable BlockPos findInterest() {
        BlockPos flower = null;
        double bestFlowerScore = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.withinManhattan(this.blockPosition(), 8, 5, 8)) {
            if (!this.level().getBlockState(pos).is(BlockTags.BEE_ATTRACTIVE) || !this.level().getBlockState(pos.above()).isAir()) {
                continue;
            }

            if (pos.distToCenterSqr(this.position()) < 2.5 * 2.5) {
                continue;
            }

            double score = pos.distToCenterSqr(this.position()) + this.random.nextDouble() * 8;
            if (score < bestFlowerScore) {
                flower = pos.immutable();
                bestFlowerScore = score;
            }
        }

        if (flower != null) {
            return flower;
        }

        // most of the time just wander instead of constantly forcing a block target
        if (this.random.nextInt(3) != 0) {
            return null;
        }

        BlockPos center = this.position().distanceToSqr(Vec3.atCenterOf(this.getHomePosition())) > 22 * 22 ? this.getHomePosition() : this.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        // otherwise pick some exposed nearby block so they still have things to inspect without flowers :p
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16, -5, -16), center.offset(16, 5, 16))) {
            if (this.level().getBlockState(pos).isAir() || !this.level().getBlockState(pos.above()).isAir()) {
                continue;
            }

            if (pos.distToCenterSqr(this.position()) < 8 * 8) {
                continue;
            }

            double score = pos.distToCenterSqr(this.position()) + this.random.nextDouble() * 60;
            if (this.level().getBlockState(pos).is(BlockTags.BEE_ATTRACTIVE)) {
                score -= 18;
            }

            if (score < bestScore) {
                best = pos.immutable();
                bestScore = score;
            }
        }

        return best;
    }

    private Vec3 wanderTarget() {
        Vec3 home = Vec3.atCenterOf(this.getHomePosition());
        Vec3 direction = this.position().distanceToSqr(home) > 24 * 24 ? home.subtract(this.position()).normalize() : this.getViewVector(0);
        for (int i = 0; i < 4; i++) {
            Vec3 target = HoverRandomPos.getPos(this, 18, 9, direction.x, direction.z, (float)Math.PI / 2, 5, 1);
            if (target == null) {
                target = AirAndWaterRandomPos.getPos(this, 18, 8, -3, direction.x, direction.z, Math.PI / 2);
            }

            if (target != null && this.position().distanceToSqr(target) > 2.5 * 2.5) {
                return target;
            }
        }

        return home.add((this.random.nextDouble() - 0.5) * 18, 1 + this.random.nextDouble() * 5, (this.random.nextDouble() - 0.5) * 18);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                  .add(Attributes.MAX_HEALTH, 10.0)
                  .add(Attributes.FLYING_SPEED, 0.1)
                  .add(Attributes.MOVEMENT_SPEED, 0.1)
                  .add(Attributes.ATTACK_DAMAGE, 2.0);
    }
}
