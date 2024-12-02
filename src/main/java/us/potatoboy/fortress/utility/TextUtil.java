package us.potatoboy.fortress.utility;


import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import us.potatoboy.fortress.Fortress;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;

public class TextUtil {
    public static MutableText getText(String type, String path, Object... values) {
        return Text.translatable(Util.createTranslationKey(type, Fortress.identifier(path)), values);
    }

    public static MutableText getTeamText(GameTeam team) {
        return getText("general", "team", team.config().name()).setStyle(Style.EMPTY.withColor(team.config().dyeColor()));
    }
}
