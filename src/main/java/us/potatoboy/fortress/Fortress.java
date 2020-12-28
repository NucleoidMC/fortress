package us.potatoboy.fortress;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import us.potatoboy.fortress.custom.FortressModules;
import us.potatoboy.fortress.game.FortressConfig;
import us.potatoboy.fortress.game.FortressWaiting;
import xyz.nucleoid.plasmid.game.GameType;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Fortress implements ModInitializer {
    public static final String ID = "fortress";
    public static final Logger LOGGER = LogManager.getLogManager().getLogger(ID);

    @Override
    public void onInitialize() {
        GameType.register(
                identifier("fortress"),
                FortressWaiting::open,
                FortressConfig.CODEC
        );

        Reflection.initialize(FortressModules.class);
    }

    public static Identifier identifier(String value) {
        return new Identifier(ID, value);
    }
}
