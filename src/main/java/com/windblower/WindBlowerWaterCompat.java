package com.windblower;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

/**
 * Optional, reflection-only bridge to Ultra Realistic Water 1.2.2.
 *
 * Ultra Realistic Water simulates its liquid/sand regions in a client-side
 * compute simulation, not as ordinary Minecraft entities. Its public
 * WaterWorld.onExplosion(Vec3, float) hook feeds a radial impulse into that
 * simulation. We place the impulse just behind the point the player is aiming
 * at, so the near side of the impulse tends to push material away from the
 * blower. This is a gust approximation, not a true per-particle directional
 * wind API.
 *
 * Reflection keeps Ultra Realistic Water optional: this class has no compile
 * dependency on that mod and harmlessly does nothing if the mod/API is absent.
 */
public final class WindBlowerWaterCompat {
    private static final String WATER_WORLD_CLASS = "me.skaffy.client.water.WaterWorld";
    private static volatile boolean warnedAboutApi;

    private WindBlowerWaterCompat() {}

    public static void pulse(Level level, Player player, Vec3 origin, Vec3 look, int remainingUseDuration) {
        if (!level.isClientSide()
                || !FabricLoader.getInstance().isModLoaded("realistic-water")
                || WindBlowerConfig.waterForce <= 0.0
                || Math.floorMod(remainingUseDuration, WindBlowerConfig.waterPulseTicks) != 0) {
            return;
        }

        try {
            Vec3 end = origin.add(look.scale(WindBlowerConfig.range));
            BlockHitResult hit = level.clip(new ClipContext(
                    origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            if (hit.getType() != HitResult.Type.BLOCK) return;

            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!isRealisticMaterial(state)) return;

            // Keep the impulse center behind the hit point. That makes the
            // near-side particles move broadly in the direction of the aim.
            float radius = (float) Math.max(0.15, Math.min(1.10, WindBlowerConfig.waterForce));
            double behind = Math.max(0.18, radius * 0.75);
            Vec3 center = hit.getLocation().subtract(look.normalize().scale(behind));
            sendWaterImpulse(center, radius);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            if (!warnedAboutApi) {
                warnedAboutApi = true;
                WindBlowerMod.LOGGER.warn(
                        "Ultra Realistic Water compatibility was unavailable; normal entity wind remains enabled. "
                                + "If the water mod updated its API, this optional bridge may need an update.",
                        exception);
            }
        }
    }

    private static boolean isRealisticMaterial(BlockState state) throws ReflectiveOperationException {
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            Class<?> worldClass = Class.forName(WATER_WORLD_CLASS);
            Method isRealistic = worldClass.getMethod("isRealistic", FluidState.class);
            Object result = isRealistic.invoke(null, fluid);
            if (Boolean.TRUE.equals(result)) return true;
        }

        // Realistic Sand is also backed by the water mod's simulated regions.
        // Class-name detection avoids linking against an optional class at compile time.
        String blockClassName = state.getBlock().getClass().getName();
        return blockClassName.endsWith(".RealisticSandBlock")
                || blockClassName.endsWith(".RealisticLavaBlock")
                || blockClassName.endsWith(".RealisticSlimeBlock");
    }

    private static void sendWaterImpulse(Vec3 center, float radius) throws ReflectiveOperationException {
        Class<?> worldClass = Class.forName(WATER_WORLD_CLASS);
        Object waterWorld = worldClass.getMethod("get").invoke(null);
        Method onExplosion = worldClass.getMethod("onExplosion", Vec3.class, float.class);

        // The water mod intentionally gates its built-in explosion impulse on
        // its tntReactions option. Respect that option instead of silently
        // changing the user's water-mod configuration.
        onExplosion.invoke(waterWorld, center, radius);
    }
}
