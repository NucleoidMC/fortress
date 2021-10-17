package us.potatoboy.fortress.utility;


import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import us.potatoboy.fortress.Fortress;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class TextUtil {
    public static MutableText getText(String type, String path, Object... values) {
        return new TranslatableText(Util.createTranslationKey(type, Fortress.identifier(path)), values);
    }

    public static MutableText getTeamText(GameTeam team) {
        return getText("general", "team", team.config().name()).setStyle(Style.EMPTY.withColor(team.config().dyeColor()));
    }
}
