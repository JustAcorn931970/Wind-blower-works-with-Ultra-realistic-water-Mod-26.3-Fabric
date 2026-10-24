package com.windblower;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class WindBlowerCommands {
    private WindBlowerCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("windblow")
                        .executes(context -> showSettings(context.getSource()))
                        .then(Commands.literal("settings")
                                .executes(context -> showSettings(context.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("setting", StringArgumentType.word())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "setting");
                                                    double value = DoubleArgumentType.getDouble(context, "value");
                                                    boolean changed = WindBlowerConfig.set(key, value);
                                                    if (changed) {
                                                        context.getSource().sendSuccess(() -> Component.literal(
                                                                "Wind Blower: " + key + " = " + WindBlowerConfig.valueFor(key)), false);
                                                        return 1;
                                                    }
                                                    context.getSource().sendFailure(Component.literal(
                                                            "Unknown setting or value out of range. Run /windblow settings for valid names and limits."));
                                                    return 0;
                                                }))))
                        .then(Commands.literal("reset")
                                .executes(context -> {
                                    WindBlowerConfig.reset();
                                    context.getSource().sendSuccess(() -> Component.literal("Wind Blower settings reset to defaults."), false);
                                    return 1;
                                }))));
    }

    private static int showSettings(net.minecraft.commands.CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Windblower settings ==="), false);
        source.sendSuccess(() -> Component.literal("range = " + WindBlowerConfig.range + " (2–24 blocks)"), false);
        source.sendSuccess(() -> Component.literal("strength = " + WindBlowerConfig.strength + " (0.02–1.50)"), false);
        source.sendSuccess(() -> Component.literal("cone = " + WindBlowerConfig.cone + " (0.20–4.00; wider cone affects more entities)"), false);
        source.sendSuccess(() -> Component.literal("lift = " + WindBlowerConfig.lift + " (0.0–0.50 upward force)"), false);
        source.sendSuccess(() -> Component.literal("maxspeed = " + WindBlowerConfig.maxSpeed + " (0.1–6.0)"), false);
        source.sendSuccess(() -> Component.literal("particles = " + WindBlowerConfig.particles + " (0–12 particles per burst)"), false);
        source.sendSuccess(() -> Component.literal("waterforce = " + WindBlowerConfig.waterForce + " (0.15–1.10 impulse radius for Ultra Realistic Water)"), false);
        source.sendSuccess(() -> Component.literal("waterpulse = " + WindBlowerConfig.waterPulseTicks + " (1–10 use ticks between water impulses; larger = less frequent)"), false);
        source.sendSuccess(() -> Component.literal("Water support requires Ultra Realistic Water's tntReactions option to be enabled."), false);
        source.sendSuccess(() -> Component.literal("Commands: /windblow set <setting> <value>  |  /windblow reset"), false);
        source.sendSuccess(() -> Component.literal("Example: /windblow set strength 0.35"), false);
        source.sendSuccess(() -> Component.literal("Water examples: /windblow set waterforce 0.35  |  /windblow set waterpulse 4"), false);
        return 1;
    }
}
