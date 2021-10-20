package us.potatoboy.fortress.game;

import net.minecraft.block.Blocks;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.Random;

public class FortressTeams {
    public static final GameTeam RED = new GameTeam(new GameTeamKey("red"), GameTeamConfig.builder()
            .setName(new TranslatableText("color.minecraft.red"))
            .setColors(GameTeamConfig.Colors.from(DyeColor.RED))
            .setFriendlyFire(false)
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .build()
    );
    public static final GameTeam BLUE = new GameTeam(new GameTeamKey("blue"), GameTeamConfig.builder()
            .setName(new TranslatableText("color.minecraft.blue"))
            .setColors(GameTeamConfig.Colors.from(DyeColor.BLUE))
            .setFriendlyFire(false)
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .build()
    );

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

    private final GameSpace gameSpace;
    private TeamManager manager;

    public FortressTeams(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    public void applyTo(GameActivity game) {
        this.manager = TeamManager.addTo(game);

        manager.addTeam(RED);
        manager.addTeam(BLUE);
    }

    public GameTeamConfig getConfig(GameTeamKey key) {
        return manager.getTeamConfig(key);
    }

    public GameTeamKey getSmallestTeam(Random random) {
        int red = manager.playersIn(RED.key()).size();
        int blue = manager.playersIn(BLUE.key()).size();

        if (red > blue) {
            return BLUE.key();
        } else if (blue > red) {
            return RED.key();
        }

        return random.nextBoolean() ? RED.key() : BLUE.key();
    }

    public void addPlayer(ServerPlayerEntity playerEntity, GameTeamKey team) {
        manager.addPlayerTo(playerEntity, team);
    }

    public void removePlayer(ServerPlayerEntity playerEntity, GameTeamKey team) {
        manager.removePlayerFrom(playerEntity, team);
    }
}
