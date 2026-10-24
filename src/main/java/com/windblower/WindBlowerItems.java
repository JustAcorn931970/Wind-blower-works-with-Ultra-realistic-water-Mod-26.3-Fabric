package com.windblower;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WindBlowerItems {
    public static final ResourceKey<Item> WIND_BLOWER_KEY = ResourceKey.create(
            Registries.ITEM, WindBlowerMod.id("wind_blower"));

    public static final Item WIND_BLOWER = Registry.register(
            BuiltInRegistries.ITEM,
            WIND_BLOWER_KEY,
            new WindBlowerItem(new Item.Properties().setId(WIND_BLOWER_KEY).stacksTo(1))
    );

    public static final ResourceKey<CreativeModeTab> WIND_BLOWER_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), WindBlowerMod.id("windblower_tab"));

    public static final CreativeModeTab WIND_BLOWER_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(WIND_BLOWER))
            .title(Component.translatable("itemGroup.windblower"))
            .displayItems((parameters, output) -> output.accept(WIND_BLOWER))
            .build();

    private WindBlowerItems() {}

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, WIND_BLOWER_TAB_KEY, WIND_BLOWER_TAB);
    }
}
