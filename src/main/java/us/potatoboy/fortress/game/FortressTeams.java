package us.potatoboy.fortress.game;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockPredicatesComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;
import us.potatoboy.fortress.custom.item.ModuleItem;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.common.team.*;
import xyz.nucleoid.plasmid.api.game.common.team.provider.DefaultTeamLists;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

import java.util.List;

public class FortressTeams {
    @Deprecated
    public static final GameTeam TEAM_1 = new GameTeam(new GameTeamKey("red"), GameTeamConfig.builder()
            .setName(Text.translatable("color.minecraft.red"))
            .setColors(GameTeamConfig.Colors.from(DyeColor.RED))
            .setFriendlyFire(false)
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .build()
    );
    @Deprecated
    public static final GameTeam TEAM_2 = new GameTeam(new GameTeamKey("blue"), GameTeamConfig.builder()
            .setName(Text.translatable("color.minecraft.blue"))
            .setColors(GameTeamConfig.Colors.from(DyeColor.BLUE))
            .setFriendlyFire(false)
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .build()
    );
    private TeamManager manager;
    private final GameTeam team1;
    private final GameTeam team2;
    private final TeamPallet team1Pallet;
    private final TeamPallet team2Pallet;

    public FortressTeams(List<GameTeam> teamList) {
        this.team1 = teamList.get(0);
        this.team2 = teamList.get(1);
        this.team1Pallet = TeamPallet.of(team1.config().blockDyeColor());
        this.team2Pallet = TeamPallet.of(team2.config().blockDyeColor());
    }

    public GameTeam getTeam1() {
        return team1;
    }

    public GameTeam getTeam2() {
        return team2;
    }

    public TeamPallet getTeam1Pallet() {
        return team1Pallet;
    }

    public TeamPallet getTeam2Pallet() {
        return team2Pallet;
    }

    public void applyTo(GameActivity game) {
        this.manager = TeamManager.addTo(game);

        manager.addTeam(this.team1);
        manager.addTeam(this.team2);
    }

    public GameTeamConfig getConfig(GameTeamKey key) {
        return manager.getTeamConfig(key);
    }

    public GameTeamKey getSmallestTeam(Random random) {
        int team1 = manager.playersIn(this.team1.key()).size();
        int team2 = manager.playersIn(this.team2.key()).size();

        if (team1 > team2) {
            return this.team2.key();
        } else if (team2 > team1) {
            return this.team1.key();
        }

        return random.nextBoolean() ? this.team1.key() : this.team2.key();
    }

    public void addPlayer(ServerPlayerEntity playerEntity, GameTeamKey team) {
        manager.addPlayerTo(playerEntity, team);
    }

    public void removePlayer(ServerPlayerEntity playerEntity, GameTeamKey team) {
        manager.removePlayerFrom(playerEntity, team);
    }

    public void giveModule(ServerPlayerEntity player, GameTeamKey team, ModuleItem item, int amount) {
        ItemStack moduleStack = new ItemStack(item, amount);
        var blockRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);

        BlockPredicate.Builder predicateBuilder = BlockPredicate.Builder.create();
        if (team == this.team1.key()) {
            predicateBuilder.blocks(blockRegistry, ColoredBlocks.concrete(team1.config().blockDyeColor()), ColoredBlocks.terracotta(team1.config().blockDyeColor()));
        } else {
            predicateBuilder.blocks(blockRegistry, ColoredBlocks.concrete(team2.config().blockDyeColor()), ColoredBlocks.terracotta(team2.config().blockDyeColor()));
        }

        moduleStack.set(DataComponentTypes.CAN_PLACE_ON, new BlockPredicatesComponent(List.of(predicateBuilder.build())));

        player.getInventory().insertStack(moduleStack);
    }
}
