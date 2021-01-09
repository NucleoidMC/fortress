package us.potatoboy.fortress.game;

import net.minecraft.block.Blocks;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class FortressTeams implements AutoCloseable{
    public static final GameTeam RED = new GameTeam("red", "Red", DyeColor.RED);
    public static final GameTeam BLUE = new GameTeam("blue", "Blue", DyeColor.BLUE);

    public static final TeamPallet RED_PALLET = new TeamPallet(
            Blocks.RED_CONCRETE,
            Blocks.RED_TERRACOTTA,
            Blocks.RED_STAINED_GLASS,
            Blocks.CRIMSON_PLANKS,
            Blocks.CRIMSON_STAIRS,
            Blocks.CRIMSON_SLAB
    );
    public static final TeamPallet BLUE_PALLET = new TeamPallet(
            Blocks.BLUE_CONCRETE,
            Blocks.BLUE_TERRACOTTA,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.WARPED_PLANKS,
            Blocks.WARPED_STAIRS,
            Blocks.WARPED_SLAB
    );

    private final ServerScoreboard scoreboard;

    final Team red;
    final Team blue;

    public FortressTeams(GameSpace gameSpace) {
        this.scoreboard = gameSpace.getServer().getScoreboard();

        this.red = createTeam(RED);
        this.blue = createTeam(BLUE);
    }

    private Team createTeam(GameTeam team) {
        Team scoreboardTeam = scoreboard.addTeam(RandomStringUtils.randomAlphabetic(16));
        scoreboardTeam.setDisplayName(new LiteralText(team.getDisplay()).formatted(team.getFormatting()));
        scoreboardTeam.setColor(team.getFormatting());
        scoreboardTeam.setFriendlyFireAllowed(false);
        scoreboardTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        return scoreboardTeam;
    }

    @Override
    public void close() throws Exception {
        scoreboard.removeTeam(this.red);
        scoreboard.removeTeam(this.blue);
    }

    public void addPlayer(ServerPlayerEntity playerEntity, GameTeam team) {
        Team scoreboardTeam = this.getScoreboardTeam(team);
        scoreboard.addPlayerToTeam(playerEntity.getEntityName(), scoreboardTeam);
    }

    public void removePlayer(ServerPlayerEntity playerEntity, GameTeam team) {
        Team scoreboardTeam = this.getScoreboardTeam(team);
        scoreboard.removePlayerFromTeam(playerEntity.getEntityName(), scoreboardTeam);
    }

    private Team getScoreboardTeam(GameTeam team) {
        return team == RED ? red : blue;
    }
}
