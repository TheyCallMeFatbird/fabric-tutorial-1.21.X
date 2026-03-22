package net.tcmfatbird.tutorialmod.item.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.tcmfatbird.tutorialmod.network.SpawnXpSpherePacket;

public class XpOrbSphereScreen extends Screen {

    private final ItemStack stack;

    private int orbCount = 200;
    private float radius = 3.5f;

    private static final int MIN_ORBS = 10;
    private static final int MAX_ORBS = 3500;
    private static final float MIN_RADIUS = 1.0f;
    private static final float MAX_RADIUS = 32.0f;

    // Warn if orbs exceed this AND radius is below this
    private static final int WARN_ORBS = 300;
    private static final float WARN_RADIUS = 3.0f;

    public XpOrbSphereScreen(ItemStack stack) {
        super(Text.literal("XP Orb Sphere"));
        this.stack = stack;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        // Orb count slider
        addDrawableChild(new SimpleSlider(cx - 100, cy - 20, 200, 20,
                "Orbs: ", orbCount,
                MIN_ORBS, MAX_ORBS, false) {
            @Override
            protected void onValueChanged(double val) {
                orbCount = (int) val;
            }
        });

        // Radius slider
        addDrawableChild(new SimpleSlider(cx - 100, cy + 10, 200, 20,
                "Radius: ", radius,
                MIN_RADIUS, MAX_RADIUS, true) {
            @Override
            protected void onValueChanged(double val) {
                radius = (float) val;
            }
        });

        // Spawn button
        addDrawableChild(ButtonWidget.builder(Text.literal("Spawn"), btn -> {
            ClientPlayNetworking.send(new SpawnXpSpherePacket(orbCount, radius));
            close();
        }).dimensions(cx - 50, cy + 40, 100, 20).build());

        // Cancel button
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> close())
                .dimensions(cx - 50, cy + 65, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("✦ XP Orb Sphere"), width / 2, height / 2 - 50, 0x55FF55);

        super.render(context, mouseX, mouseY, delta);

        if (orbCount > WARN_ORBS && radius < WARN_RADIUS) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("⚠ High orb count at low radius may cause lag!").styled(s ->
                            s.withColor(net.minecraft.util.Formatting.RED).withBold(true)),
                    width / 2, height / 2 + 35, 0xFF4444);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ── Generic slider ──────────────────────────────────────────────────────

    private abstract static class SimpleSlider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final boolean decimal;

        public SimpleSlider(int x, int y, int width, int height,
                            String label, double initial, double min, double max, boolean decimal) {
            super(x, y, width, height, Text.empty(), (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimal = decimal;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + value * (max - min);
            String display = decimal
                    ? String.format("%.1f", val)
                    : String.valueOf((int) Math.round(val));
            setMessage(Text.literal(label + display));
        }

        @Override
        protected void applyValue() {
            onValueChanged(min + value * (max - min));
        }

        protected abstract void onValueChanged(double val);
    }
}