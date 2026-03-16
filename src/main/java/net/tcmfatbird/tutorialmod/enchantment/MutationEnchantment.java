package net.tcmfatbird.tutorialmod.enchantment;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.tcmfatbird.tutorialmod.TutorialMod;

import java.util.List;
import java.util.Random;

public class MutationEnchantment {

    // Armor items grouped by slot [HEAD, CHEST, LEGS, FEET]
    private static final List<Item[]> ARMOR_SETS = List.of(
            // HEAD, CHEST, LEGS, FEET
            new Item[]{ Items.LEATHER_HELMET,   Items.LEATHER_CHESTPLATE,   Items.LEATHER_LEGGINGS,   Items.LEATHER_BOOTS   },
            new Item[]{ Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS },
            new Item[]{ Items.IRON_HELMET,      Items.IRON_CHESTPLATE,      Items.IRON_LEGGINGS,      Items.IRON_BOOTS      },
            new Item[]{ Items.GOLDEN_HELMET,    Items.GOLDEN_CHESTPLATE,    Items.GOLDEN_LEGGINGS,    Items.GOLDEN_BOOTS    },
            new Item[]{ Items.DIAMOND_HELMET,   Items.DIAMOND_CHESTPLATE,   Items.DIAMOND_LEGGINGS,   Items.DIAMOND_BOOTS   },
            new Item[]{ Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS }
    );

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,   // index 0
            EquipmentSlot.CHEST,  // index 1
            EquipmentSlot.LEGS,   // index 2
            EquipmentSlot.FEET    // index 3
    };

    private static int slotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD  -> 0;
            case CHEST -> 1;
            case LEGS  -> 2;
            case FEET  -> 3;
            default    -> -1;
        };
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, net.minecraft.entity.damage.DamageSource source, float amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;
            if (!(entity.getWorld() instanceof ServerWorld)) return true;
            if (amount <= 0) return true;

            Random random = new Random();
            var registryManager = entity.getWorld().getRegistryManager();
            var enchantRegistry = registryManager.get(RegistryKeys.ENCHANTMENT);

            RegistryKey<Enchantment> mutationKey = RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    Identifier.of(TutorialMod.MOD_ID, "mutation")
            );

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack currentArmor = player.getEquippedStack(slot);
                if (currentArmor.isEmpty()) continue;
                if (!(currentArmor.getItem() instanceof ArmorItem)) continue;

                // Check if this piece has Mutation
                ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(currentArmor);
                boolean hasMutation = enchantments.getEnchantments().stream()
                        .anyMatch(entry -> entry.getKey().map(k -> k.equals(mutationKey)).orElse(false));

                if (!hasMutation) continue;

                int idx = slotIndex(slot);
                if (idx == -1) continue;

                // Find which set the current item belongs to
                int currentSet = -1;
                for (int i = 0; i < ARMOR_SETS.size(); i++) {
                    if (ARMOR_SETS.get(i)[idx] == currentArmor.getItem()) {
                        currentSet = i;
                        break;
                    }
                }

                // Pick a random different set
                int newSet;
                do {
                    newSet = random.nextInt(ARMOR_SETS.size());
                } while (newSet == currentSet);

                Item newItem = ARMOR_SETS.get(newSet)[idx];
                ItemStack newStack = new ItemStack(newItem);

                // Copy all enchantments (including Mutation) to the new piece
                ItemEnchantmentsComponent.Builder builder =
                        new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                enchantments.getEnchantments().forEach(entry ->
                        builder.add(entry, enchantments.getLevel(entry))
                );
                newStack.set(DataComponentTypes.ENCHANTMENTS, builder.build());

                player.equipStack(slot, newStack);
            }

            return true;
        });
    }
}