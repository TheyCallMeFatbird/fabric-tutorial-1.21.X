package net.tcmfatbird.tutorialmod.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.tcmfatbird.tutorialmod.TutorialMod;

public class ModOreGeneration {
    public static final RegistryKey<PlacedFeature> URANIUM_ORE_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(TutorialMod.MOD_ID, "uranium_ore_placed")
    );

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                URANIUM_ORE_PLACED_KEY
        );
    }
}
