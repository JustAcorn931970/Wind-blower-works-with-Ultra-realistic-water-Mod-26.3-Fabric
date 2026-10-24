package com.windblower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Continuous wind emitter. Entity velocity is changed only on the logical server. */
public final class WindBlowerItem extends Item {
    public WindBlowerItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(user instanceof Player player) || player.isSpectator()) return;

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition();

        if (level.isClientSide()) {
            spawnWindParticles(level, origin, look, remainingUseDuration);
            return;
        }

        try {
            pushEntitiesAlongBeam(level, player, origin, look);
        } catch (RuntimeException exception) {
            // Contain ordinary per-tick logic mistakes so they do not repeatedly crash the tick loop.
            // The exception is recorded in latest.log for diagnosis.
            WindBlowerMod.LOGGER.error("Wind Blower failed while applying wind; stopping this use.", exception);
            player.stopUsingItem();
        }
    }

    private static void pushEntitiesAlongBeam(Level level, Player player, Vec3 origin, Vec3 look) {
        double range = WindBlowerConfig.range;
        Vec3 end = origin.add(look.scale(range));
        BlockHitResult wall = level.clip(new ClipContext(
                origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double clearRange = range;
        if (wall.getType() == HitResult.Type.BLOCK) {
            clearRange = Math.min(range, origin.distanceTo(wall.getLocation()));
        }
        if (clearRange < 0.25) return;

        Vec3 beamEnd = origin.add(look.scale(clearRange));
        AABB search = new AABB(origin, beamEnd).inflate(WindBlowerConfig.cone + 1.25);
        List<Entity> targets = level.getEntitiesOfClass(Entity.class, search, entity -> entity != player && !entity.isRemoved());

        for (Entity target : targets) {
            if (target == player || target.isRemoved()) continue;

            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 offset = center.subtract(origin);
            double along = offset.dot(look);
            if (along < 0.2 || along > clearRange + Math.max(0.4, target.getBbWidth())) continue;

            Vec3 pointOnBeam = origin.add(look.scale(along));
            double sideSq = center.subtract(pointOnBeam).lengthSqr();
            double width = WindBlowerConfig.cone * (0.35 + 0.65 * (along / range))
                    + Math.max(0.25, target.getBbWidth() * 0.40);
            if (sideSq > width * width) continue;

            double distanceFactor = Math.max(0.25, 1.0 - (along / range) * 0.42);
            Vec3 velocity = target.getDeltaMovement();
            double forwardVelocity = velocity.dot(look);
            double needed = Math.max(0.0, WindBlowerConfig.maxSpeed - forwardVelocity);
            double impulse = Math.min(WindBlowerConfig.strength, needed * 0.30) * distanceFactor;
            if (impulse <= 0.0001 && WindBlowerConfig.lift <= 0.0001) continue;

            // setDeltaMovement is used rather than a newer convenience overload, keeping this
            // compatible with the version's ordinary Entity velocity API.
            target.setDeltaMovement(velocity.add(look.scale(impulse))
                    .add(0.0, WindBlowerConfig.lift * distanceFactor, 0.0));
        }
    }

    private static void spawnWindParticles(Level level, Vec3 eye, Vec3 look, int remaining) {
        int count = WindBlowerConfig.particles;
        if (count <= 0 || (remaining & 1) != 0) return;

        RandomSource random = level.getRandom();
        Vec3 referenceUp = Math.abs(look.y) < 0.90 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 right = look.cross(referenceUp).normalize();
        Vec3 up = right.cross(look).normalize();
        Vec3 muzzle = eye.add(look.scale(0.62));

        for (int i = 0; i < count; i++) {
            double distance = 0.15 + random.nextDouble() * 0.55;
            double side = (random.nextDouble() - 0.5) * 0.34;
            double vertical = (random.nextDouble() - 0.5) * 0.28;
            Vec3 position = muzzle.add(look.scale(distance)).add(right.scale(side)).add(up.scale(vertical));
            Vec3 velocity = look.scale(0.23 + random.nextDouble() * 0.18)
                    .add(right.scale((random.nextDouble() - 0.5) * 0.08))
                    .add(up.scale((random.nextDouble() - 0.5) * 0.08));
            level.addParticle(ParticleTypes.CLOUD, position.x, position.y, position.z,
                    velocity.x, velocity.y, velocity.z);
            if ((i & 1) == 0) {
                level.addParticle(ParticleTypes.CRIT, position.x, position.y, position.z,
                        velocity.x * 0.55, velocity.y * 0.55, velocity.z * 0.55);
            }
        }
    }
}
