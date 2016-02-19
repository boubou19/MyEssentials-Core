package myessentials.chat.api;

import myessentials.exception.FormatException;
import myessentials.utils.ColorUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang.StringUtils;

/**
 * An IChatComponent that converts a format to a set of IChatComponents
 *
 * Each of the parenthesis pairs represent an IChatComponent with its
 * own ChatStyle (color, bold, underlined, etc.)
 *
 * Example: {2|Entity number }{%s}{2| is the }{7l| %s}
 * This format will create the following IChatComponents:
 *  - "Entity number "; with DARK_GREEN color
 *  - %s; one of the parameters sent by the caller (as IChatComponent or
 *                      IChatFormat, since it's missing the "|" style delimiter character
 *  - " is the "; with DARK_GREEN color
 *  - %s; one of the parameters sent by the caller (as String since it HAS "|" style delimiter character)
 */
public class ChatComponentFormatted extends ChatComponentText {

    public ChatComponentFormatted(String format, Object... args) {
        super("");

        String[] components = StringUtils.split(format, "{}");
        int argNumber = 0;

        for (String component : components) {
            String[] parts = component.split("\\|");

            if(parts.length == 2) {
                ChatStyle chatStyle = getStyle(parts[0]);

                String actualText = parts[1];
                while (actualText.contains("%s")) {
                    actualText = actualText.replaceFirst("%s", args[argNumber++].toString());
                }
                //text += actualText;
                this.appendSibling(new ChatComponentText(actualText).setChatStyle(chatStyle));

            } else if (parts.length == 1 && parts[0].equals("%s")) {

                if (args[argNumber] instanceof IChatFormat) {
                    IChatComponent formattedArgument = ((IChatFormat) args[argNumber++]).toChatMessage();
                    //this.text += formattedArgument.getUnformattedText();
                    this.appendSibling(formattedArgument);
                } else if (args[argNumber] instanceof IChatComponent) {
                    this.appendSibling((IChatComponent) args[argNumber++]);
                } else {
                    throw new FormatException("Argument at position " + argNumber + " does not implement IChatFormat interface");
                }

            } else {
                throw new FormatException("Format " + component + " is not valid. Valid format: [modifiers|text]");
            }
        }
    }

    /**
     * Converts the modifiers String to a ChatStyle
     * [modifiers|  some text]
     *  ^^^^^^^^    ^^^^^^^^^
     *  STYLE  for  THIS TEXT
     */
    private ChatStyle getStyle(String modifiers) {

        ChatStyle chatStyle = new ChatStyle();

        for (char c : modifiers.toCharArray()) {
            applyModifier(chatStyle, c);
        }

        return chatStyle;
    }

    /**
     * Applies modifier to the style
     * Returns whether or not the modifier was valid
     */
    private boolean applyModifier(ChatStyle chatStyle, char modifier) {
        if (modifier >= '0' && modifier <= '9' || modifier >= 'a' && modifier <= 'f') {
            chatStyle.setColor(ColorUtils.colorMap.get(modifier));
            return true;
        }
        switch (modifier) {
            case 'k': chatStyle.setObfuscated(true); return true;
            case 'l': chatStyle.setBold(true); return true;
            case 'm': chatStyle.setStrikethrough(true); return true;
            case 'n': chatStyle.setUnderlined(true); return true;
            case 'o': chatStyle.setItalic(true); return true;
        }
        return false;
    }



    @Override
    public String getUnformattedTextForChat() {
        return "";
    }
}