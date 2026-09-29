package co.bracesoftware.libs.minecraft_text_formatter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import java.util.Optional;

public final class Comptofs
{
    public static final String toString(Component c)
    {
        var b = new StringBuilder();

        c.visit(
            (style, text) -> {
                appsc(b, style);
                b.append(text);
                return Optional.empty();
            }, Style.EMPTY
        );

        return b.toString();
    }

    public static final void appsc(StringBuilder b, Style s)
    {
        if(s == null) return;
        var col = s.getColor();
        if(col != null)
        {
            var ff = ChatFormatting.getByName(col.serialize());
            if(ff != null)
            {
                b.append("§").append(ff.getChar());
            }
            else
            {
                var hex = String.format("%06X", col.getValue());
                b.append("§x");
                for(char c : hex.toCharArray())
                {
                    b.append("§").append(c);
                }
            }
        }

        if(s.isBold()) b.append("§l");
        if(s.isItalic()) b.append("§o");
        if(s.isUnderlined()) b.append("§n");
        if(s.isStrikethrough()) b.append("§m");
        if(s.isObfuscated()) b.append("§k");
        return;
    }
}