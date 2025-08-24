package us.potatoboy.fortress.game.active;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import us.potatoboy.fortress.game.FortressConfig;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;

public class FortressStateManager {
    private final FortressActive game;

    private long closeTime = -1;
    public long finishTime = -1;

    FortressStateManager(FortressActive game) {
        this.game = game;
    }

    public void onOpen(long time, FortressConfig config) {
        finishTime = time + (config.timeLimitMins() * 20L * 60L);
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
            if (winner == game.teams.getTeam1()) {
                return TickResult.TEAM_1_WIN;
            } else {
                return TickResult.TEAM_2_WIN;
            }
        }

        return TickResult.CONTINUE_TICK;
    }

    public GameTeam testWin(long time) {
        Pair<Integer, Integer> percents = game.getMap().getControlPercent();
        int team1Percent = percents.getLeft();
        int team2Percent = percents.getRight();

        if (time >= finishTime || !game.config.recapture() && team1Percent + team2Percent == 100) {
            if (team1Percent == team2Percent) {
                return null;
            }

            if (team1Percent > team2Percent) {
                return game.teams.getTeam1();
            } else {
                return game.teams.getTeam2();
            }
        }

        if (team2Percent == 0) {
            return game.teams.getTeam1();
        }

        if (team1Percent == 0) {
            return game.teams.getTeam2();
        }

        return getRemainingTeam();
    }

    private GameTeam getRemainingTeam() {
        boolean team1Remaining = false;
        boolean team2Remaining = false;

        for (ServerPlayerEntity player : game.gameSpace.getPlayers()) {
            FortressPlayer participant = game.getParticipant(player);
            if (participant != null) {
                if (participant.team == game.teams.getTeam1().key()) {
                    team1Remaining = true;
                } else if (participant.team == game.teams.getTeam2().key()) {
                    team2Remaining = true;
                }
            }
        }

        if (team1Remaining && !team2Remaining) {
            return game.teams.getTeam1();
        }

        if (team2Remaining && !team1Remaining) {
            return game.teams.getTeam2();
        }

        return null;
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
        TEAM_1_WIN,
        TEAM_2_WIN,
        GAME_CLOSED
    }
}
