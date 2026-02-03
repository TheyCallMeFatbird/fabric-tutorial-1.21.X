package net.tcmfatbird.tutorialmod.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.tcmfatbird.tutorialmod.network.SetTimePacket;

public class ClockScreen extends Screen {

    private long currentTime;
    private long sliderTime;
    private TimeSlider slider;

    public ClockScreen() {
        super(Text.literal("Clock"));
    }

    @Override
    protected void init() {
        currentTime = client.world.getTimeOfDay() % 24000;
        sliderTime = currentTime;

        slider = new TimeSlider(
                width / 2 - 150,
                height / 2 - 10,
                300,
                20,
                currentTime / 24000.0
        );
        addDrawableChild(slider);

        // Set Time button
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Time"), (btn) -> {
            ClientPlayNetworking.send(new SetTimePacket(sliderTime));
            currentTime = sliderTime;
        }).dimensions(width / 2 - 50, height / 2 + 30, 100, 20).build());

        // Reset button
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), (btn) -> {
            sliderTime = currentTime;
            slider.setValue(currentTime / 24000.0);
        }).dimensions(width / 2 + 55, height / 2 + 30, 60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("⏰ Clock"), width / 2, height / 2 - 45, 0xFFFFFF);

        // Time label (updates live as slider moves)
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Time: " + formatTime(sliderTime)), width / 2, height / 2 - 25, 0xFFD700);

        super.render(context, mouseX, mouseY, delta);
    }

    public void onSliderChange(double value) {
        sliderTime = (long) (value * 23999);
    }

    private static String formatTime(long ticks) {
        int hours = (int) ((ticks + 6000) % 24000 / 1000);
        int minutes = (int) (((ticks + 6000) % 1000) * 60 / 1000);
        return String.format("%02d:%02d", hours, minutes);
    }

    // ─── Custom slider ────────────────────────────────────────────────────────

    private class TimeSlider extends SliderWidget {

        public TimeSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Text.literal(""), value);
        }

        @Override
        protected void applyValue() {
            ClockScreen.this.onSliderChange(this.value);
        }

        @Override
        protected void updateMessage() {
            // This sets the label displayed on the slider itself
            this.setMessage(Text.literal(formatTime((long) (this.value * 23999))));
        }

        public void setValue(double newValue) {
            this.value = newValue;
            applyValue();
            updateMessage();
        }
    }
}