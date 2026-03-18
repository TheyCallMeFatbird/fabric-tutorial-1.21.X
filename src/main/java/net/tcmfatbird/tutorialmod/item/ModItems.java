package net.tcmfatbird.tutorialmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tcmfatbird.tutorialmod.TutorialMod;
import net.tcmfatbird.tutorialmod.item.custom.ChiselItem;
import net.tcmfatbird.tutorialmod.item.custom.QuantumTunnelerItem;
import net.tcmfatbird.tutorialmod.item.custom.RainbowDyeItem;
import net.tcmfatbird.tutorialmod.item.custom.ThickenedInkItem;
import net.minecraft.text.Text;

import java.util.List;

public class ModItems {
    public static final Item PINK_GARNET = registerItem("pink_garnet", new Item(new Item.Settings()));
    public static final Item RAW_PINK_GARNET = registerItem("raw_pink_garnet", new Item(new Item.Settings()));

    public static final Item CHISEL = registerItem("chisel", new ChiselItem(new Item.Settings().maxDamage(32)));

    public static final Item THICKENED_INK = registerItem("thickened_ink", new ThickenedInkItem(new Item.Settings()));

    public static final Item RAINBOW_DYE = registerItem("rainbow_dye", new RainbowDyeItem(new Item.Settings()));

    public static final Item CAULIFLOWER = registerItem("cauliflower", new Item(new Item.Settings().food(ModFoodComponents.CAULIFLOWER)) {
        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.tutorialmod.cauliflower.tooltip"));
            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static final Item STARLIGHT_ASHES = registerItem("starlight_ashes", new Item(new Item.Settings()));

    public static final Item GEIGER_COUNTER = registerItem("geiger_counter", new Item(new Item.Settings()));

    public static final Item QUANTUM_TUNNELER = registerItem("quantum_tunneler", new QuantumTunnelerItem(new Item.Settings().maxDamage(16)));


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(TutorialMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        TutorialMod.LOGGER.info("Registering Mod Items for " + TutorialMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(PINK_GARNET);
            fabricItemGroupEntries.add(RAW_PINK_GARNET);
            fabricItemGroupEntries.add(GEIGER_COUNTER);
            fabricItemGroupEntries.add(QUANTUM_TUNNELER);
        });
    }
}
