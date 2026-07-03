package us.potatoboy.fortress;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import us.potatoboy.fortress.custom.block.FortressBlocks;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.game.FortressConfig;
import us.potatoboy.fortress.game.FortressWaiting;
import xyz.nucleoid.plasmid.api.game.GameTypes;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Fortress implements ModInitializer {
    public static final String ID = "fortress";
    public static final Logger LOGGER = LogManager.getLogManager().getLogger(ID);

    public static final Style PREFIX_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x858585));

    @Override
    public void onInitialize() {
        GameTypes.register(
                identifier("fortress"),
                FortressConfig.CODEC,
                FortressWaiting::open
        );

        Reflection.initialize(FortressModules.class);
        Reflection.initialize(FortressBlocks.class);
    }

    public static Identifier identifier(String value) {
        return Identifier.fromNamespaceAndPath(ID, value);
    }
}
