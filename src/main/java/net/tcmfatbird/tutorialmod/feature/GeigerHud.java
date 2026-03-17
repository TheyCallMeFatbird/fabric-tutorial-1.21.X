package net.tcmfatbird.tutorialmod.feature;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.tcmfatbird.tutorialmod.item.ModItems;

public class GeigerHud {

    public static void register() {
        HudRenderCallback.EVENT.register(GeigerHud::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean holdingGeiger = client.player.getMainHandStack().isOf(ModItems.GEIGER_COUNTER)
                || client.player.getOffHandStack().isOf(ModItems.GEIGER_COUNTER);
        if (!holdingGeiger) return;

        int level = GeigerCounterClient.getRadiationLevel();
        int screenWidth = context.getScaledWindowWidth();
        int x = screenWidth / 2 - 51;
        int y = 10;

        // Background box
        context.fill(x - 2, y - 2, x + 104, y + 24, 0xAA000000);

        // Title
        context.drawText(client.textRenderer, "§a☢ GEIGER COUNTER", x + 4, y, 0x00FF00, false);

        // Bar background
        context.fill(x, y + 10, x + 100, y + 18, 0xFF333333);

        // Bar fill
        int barColor = level == 0 ? 0xFF00FF00 : level == 1 ? 0xFFFFAA00 : 0xFFFF0000;
        int barWidth = level == 0 ? 10 : level == 1 ? 55 : 100;
        context.fill(x, y + 10, x + barWidth, y + 18, barColor);

        // Distance text
        int distance = GeigerCounterClient.getNearestDistance();
        String distText = distance == Integer.MAX_VALUE ? "§aNO SIGNAL" : "§f" + distance + "m";
        context.drawText(client.textRenderer, distText, x + 104 + 4, y + 10, 0xFFFFFF, false);
    }
}