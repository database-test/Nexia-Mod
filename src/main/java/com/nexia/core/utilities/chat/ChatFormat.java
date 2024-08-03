package com.nexia.core.utilities.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ChatFormat {

    // Colors

    public static TextColor chatColor2 = Minecraft.gray;

    public static TextColor normalColor = Minecraft.white;
    public static TextColor systemColor = chatColor2;

    public static TextColor brandColor1 = TextColor.fromHexString("#a400fc");

    public static TextColor brandColor2 = TextColor.fromHexString("#e700f0");

    public static TextColor failColor = TextColor.fromHexString("#ff2b1c");

    public static TextColor greenColor = TextColor.fromHexString("#38e312");

    public static TextColor goldColor = TextColor.fromHexString("#f5bc42");

    public static TextColor lineColor = Minecraft.dark_gray;

    public static TextColor arrowColor = TextColor.fromHexString("#4a4a4a");
    public static TextColor lineTitleColor = brandColor1;

    // Decorations

    public static TextDecoration bold = TextDecoration.BOLD;
    public static TextDecoration italic = TextDecoration.ITALIC;
    public static TextDecoration strikeThrough = TextDecoration.STRIKETHROUGH;
    public static TextDecoration underlined = TextDecoration.UNDERLINED;
    public static TextDecoration obfuscated = TextDecoration.OBFUSCATED;

    // Minecraft Chat Colours (but better)

    public class Minecraft {
        public static TextColor dark_red = TextColor.fromHexString("#b51413");

        public static TextColor red = TextColor.fromHexString("#ff2b1c");

        public static TextColor gold = TextColor.fromHexString("#f5bc42");

        public static TextColor yellow = TextColor.fromHexString("#fff60c");

        public static TextColor dark_green = TextColor.fromHexString("#1b9c19");

        public static TextColor green = TextColor.fromHexString("#38e312");

        public static TextColor aqua = TextColor.fromHexString("#28e2ff");

        public static TextColor dark_aqua = TextColor.fromHexString("#399ca9");

        public static TextColor dark_blue = TextColor.fromHexString("#263daa");

        public static TextColor blue = TextColor.fromHexString("#2955ff");

        public static TextColor light_purple = TextColor.fromHexString("#e700f0");

        public static TextColor dark_purple = TextColor.fromHexString("#a400fc");

        public static TextColor white = TextColor.fromHexString("#ffffff");

        public static TextColor gray = TextColor.fromHexString("#c7c7c7");

        public static TextColor dark_gray = TextColor.fromHexString("#767676");

        public static TextColor black = TextColor.fromHexString("#252525");
    }

    // Text

    public static Component nexia = MiniMessage.get().parse(String.format("<bold><gradient:%s:%s>Nexia</gradient></bold>", ChatFormat.brandColor1, ChatFormat.brandColor2));

    public static Component nexiaMessage = nexia.append(MiniMessage.get().parse(String.format(" <color:%s>»</color> ", ChatFormat.arrowColor)));


    public static Component separatorLine(String title) {
        String spaces = "                                                                ";

        if (title != null) {
            int lineLength = spaces.length() - Math.round((float)title.length() * 1.33F) - 4;
            return Component.text(spaces.substring(0, lineLength / 2)).color(lineColor).decorate(strikeThrough)
                    .append(Component.text("[ ").color(lineColor).decoration(strikeThrough, false))
                    .append(Component.text(title).color(lineTitleColor).decoration(strikeThrough, false))
                    .append(Component.text(" ]").color(lineColor).decoration(strikeThrough, false))
                    .append(Component.text(spaces.substring(0, (lineLength + 1) / 2))).color(lineColor).decorate(strikeThrough);
        } else {
            return Component.text(spaces).color(lineColor).decorate(strikeThrough);
        }
    }
}
