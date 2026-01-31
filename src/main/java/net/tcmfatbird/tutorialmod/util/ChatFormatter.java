package net.tcmfatbird.tutorialmod.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {

    private static final Map<Character, Formatting> MAP = new HashMap<>();
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("&gradient\\{([0-9A-Fa-f]{6})-([0-9A-Fa-f]{6})}(.+?)(?=&(?!#)|$)");

    static {
        MAP.put('0', Formatting.BLACK);
        MAP.put('1', Formatting.DARK_BLUE);
        MAP.put('2', Formatting.DARK_GREEN);
        MAP.put('3', Formatting.DARK_AQUA);
        MAP.put('4', Formatting.DARK_RED);
        MAP.put('5', Formatting.DARK_PURPLE);
        MAP.put('6', Formatting.GOLD);
        MAP.put('7', Formatting.GRAY);
        MAP.put('8', Formatting.DARK_GRAY);
        MAP.put('9', Formatting.BLUE);
        MAP.put('a', Formatting.GREEN);
        MAP.put('b', Formatting.AQUA);
        MAP.put('c', Formatting.RED);
        MAP.put('d', Formatting.LIGHT_PURPLE);
        MAP.put('e', Formatting.YELLOW);
        MAP.put('f', Formatting.WHITE);
        MAP.put('k', Formatting.OBFUSCATED);
        MAP.put('l', Formatting.BOLD);
        MAP.put('m', Formatting.STRIKETHROUGH);
        MAP.put('n', Formatting.UNDERLINE);
        MAP.put('o', Formatting.ITALIC);
        MAP.put('r', Formatting.RESET);
    }

    // Tracks all active formatting modifiers independently of color
    private static class StyleState {
        TextColor color = TextColor.fromRgb(0xFFFFFF);
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        Style build() {
            Style style = Style.EMPTY.withColor(color);
            if (bold) style = style.withBold(true);
            if (italic) style = style.withItalic(true);
            if (underline) style = style.withUnderline(true);
            if (strikethrough) style = style.withStrikethrough(true);
            if (obfuscated) style = style.withObfuscated(true);
            return style;
        }

        void reset() {
            color = TextColor.fromRgb(0xFFFFFF);
            bold = false;
            italic = false;
            underline = false;
            strikethrough = false;
            obfuscated = false;
        }

        void applyFormatting(Formatting f) {
            switch (f) {
                case BOLD:          bold = true;          break;
                case ITALIC:        italic = true;        break;
                case UNDERLINE:     underline = true;     break;
                case STRIKETHROUGH: strikethrough = true; break;
                case OBFUSCATED:    obfuscated = true;    break;
                case RESET:         reset();              break;
                default:
                    // It's a color code
                    color = TextColor.fromFormatting(f);
                    break;
            }
        }
    }

    public static MutableText parse(String input, boolean allowMagic) {
        // Process markdown-style formatting first
        input = processMarkdown(input);
        // Then process gradients
        input = processGradients(input);

        MutableText text = Text.empty();
        StyleState state = new StyleState();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);

                // Hex color code: &#RRGGBB
                if (next == '#' && i + 7 < input.length()) {
                    String hexCode = input.substring(i + 2, i + 8);
                    try {
                        int color = Integer.parseInt(hexCode, 16);
                        state.color = TextColor.fromRgb(color);
                        i += 7;
                        continue;
                    } catch (NumberFormatException e) {
                        // Invalid hex, fall through to normal text
                    }
                }

                // Standard formatting/color code
                Formatting f = MAP.get(Character.toLowerCase(next));
                if (f != null) {
                    if (!allowMagic && f == Formatting.OBFUSCATED) {
                        i++;
                        continue;
                    }
                    state.applyFormatting(f);
                    i++;
                    continue;
                }
            }

            text.append(Text.literal(String.valueOf(c)).setStyle(state.build()));
        }

        return text;
    }

    // Converts *text* -> &otext&r and **text** -> &ltext&r
    private static String processMarkdown(String input) {
        // Process ** bold ** first so we don't confuse it with single *
        input = input.replaceAll("\\*\\*(.+?)\\*\\*", "&l$1&r");
        // Then process * italic *
        input = input.replaceAll("\\*(.+?)\\*", "&o$1&r");
        return input;
    }

    private static String processGradients(String input) {
        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);

            String gradientText = createGradient(text, startHex, endHex);
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        if (text.isEmpty()) return "";

        int startColor = Integer.parseInt(startHex, 16);
        int endColor = Integer.parseInt(endHex, 16);

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        // Collect only the printable characters (skip any & codes inside the gradient text)
        // so we can calculate gradient ratio correctly
        StringBuilder printableChars = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '#' && i + 7 < text.length()) {
                    i += 7; // skip &#RRGGBB
                    continue;
                }
                if (MAP.containsKey(Character.toLowerCase(next))) {
                    i++; // skip &X
                    continue;
                }
            }
            printableChars.append(c);
        }

        int totalPrintable = printableChars.length();
        int printableIndex = 0;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Pass through any & formatting codes untouched
            if (c == '&' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '#' && i + 7 < text.length()) {
                    // This is a hex code inside gradient — skip it, gradient owns the color
                    i += 7;
                    continue;
                }
                if (MAP.containsKey(Character.toLowerCase(next))) {
                    Formatting f = MAP.get(Character.toLowerCase(next));
                    // Only pass through non-color formatting codes (bold, italic, etc.)
                    if (!f.isColor() && f != Formatting.RESET) {
                        result.append('&').append(next);
                    }
                    i++;
                    continue;
                }
            }

            if (c == ' ') {
                result.append(' ');
                printableIndex++;
                continue;
            }

            float ratio = totalPrintable <= 1 ? 0 : (float) printableIndex / (totalPrintable - 1);

            int r = (int) (startR + ratio * (endR - startR));
            int g = (int) (startG + ratio * (endG - startG));
            int b = (int) (startB + ratio * (endB - startB));

            result.append(String.format("&#%02X%02X%02X%c", r, g, b, c));
            printableIndex++;
        }

        return result.toString();
    }

    public static MutableText parseForPlayer(String input, ServerPlayerEntity player) {
        boolean allowMagic = player.hasPermissionLevel(2);
        return parse(input, allowMagic);
    }
}