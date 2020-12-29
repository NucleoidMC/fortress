package us.potatoboy.fortress.game.active;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import us.potatoboy.fortress.game.FortressConfig;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class FortressStateManager {
    private final FortressActive game;

    private long closeTime = -1;
    public long finishTime = -1;

    FortressStateManager(FortressActive game) {
        this.game = game;
    }

    public void onOpen(long time, FortressConfig config) {
        finishTime = time + (config.timeLimitMins * 20 * 60);
    }

    public TickResult tick(long time) {
        if (game.gameSpace.getPlayers().isEmpty()) {
            return TickResult.GAME_CLOSED;
        }

        if (this.closeTime > 0) {
            return tickClosing(time);
        }

        GameTeam winner = testWin(time);
        if (winner != null) {
            triggerFinish(time);
            if (winner == FortressTeams.RED) {
                return TickResult.RED_WIN;
            } else {
                return TickResult.BLUE_WIN;
            }
        }

        return TickResult.CONTINUE_TICK;
    }

    public GameTeam testWin(long time) {
        Pair<Integer, Integer> percents = game.getMap().getControlPercent();
        int redPercent = percents.getLeft();
        int bluePercent = percents.getRight();

        if (time >= finishTime) {
            if (redPercent == bluePercent) {
                return null;
            }

            if (redPercent > bluePercent) {
                return FortressTeams.RED;
            } else {
                return FortressTeams.BLUE;
            }
        }

        if (bluePercent == 0) {
            return FortressTeams.RED;
        }

        if (redPercent == 0) {
            return FortressTeams.BLUE;
        }

        if (game.gameSpace.getPlayerCount() <= 1) {
            GameTeam team = getRemainingTeam();
            return team;
        }

        return null;
    }

    private GameTeam getRemainingTeam() {
        for (ServerPlayerEntity player : game.gameSpace.getPlayers()) {
            FortressPlayer participant = game.getParticipant(player);
            if (participant != null) {
                return participant.team;
            }
        }

        return FortressTeams.RED;
    }

    public void triggerFinish(long time) {
        closeTime = time + (10 * 20);
    }

    public TickResult tickClosing(long time) {
        if (time >= closeTime) {
            return TickResult.GAME_CLOSED;
        }

        return TickResult.TICK_FINISHED;
    }

    public enum TickResult {
        CONTINUE_TICK,
        TICK_FINISHED,
        RED_WIN,
        BLUE_WIN,
        GAME_CLOSED
    }
}
