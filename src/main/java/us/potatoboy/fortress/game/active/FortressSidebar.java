package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.text.Format;
import java.util.HashMap;
import java.util.Map;

public class FortressSidebar {
    private final FortressActive game;
    public final HashMap<FortressPlayer, SidebarWidget> sidebars = new HashMap<>();

    FortressSidebar(FortressActive game, GlobalWidgets widgets) {
        this.game = game;

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            SidebarWidget scoreboard = new SidebarWidget(game.gameSpace, new LiteralText("Fortress").formatted(Formatting.BOLD));

            scoreboard.addPlayer(entry.getKey().getEntity(game.gameSpace.getWorld()));

            sidebars.put(entry.getValue(), scoreboard);
        }
    }

    public void update(long time) {
        for (Map.Entry<FortressPlayer, SidebarWidget> entry : sidebars.entrySet()) {
            SidebarWidget sidebar = entry.getValue();
            FortressPlayer participant = entry.getKey();

            sidebar.set(content -> {
                long ticksUntilEnd = game.stateManager.finishTime - time;
                content.writeLine(formatTimeLeft(ticksUntilEnd));
                content.writeLine("");
                content.writeLine("");

                Pair percents = game.getMap().getControlPercent();

                content.writeLine(Formatting.RED + "Red:  " + Formatting.GREEN + percents.getLeft() + "%");
                content.writeLine(Formatting.BLUE + "Blue: " + Formatting.GREEN + percents.getRight() + "%");
                content.writeLine("");
                content.writeLine("Kills: " + Formatting.GREEN + participant.kills);
                content.writeLine("Captures: " + Formatting.GREEN + participant.captures);
            });
        }
    }

    public static String formatTimeLeft(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / 20;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;

        return String.format("Time Left: %s%02d:%02d", Formatting.GREEN, minutes, seconds);
    }

    public void close() {
        for (SidebarWidget sidebar : sidebars.values()) {
            sidebar.close();
        }
    }
}
