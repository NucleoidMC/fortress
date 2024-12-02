package us.potatoboy.fortress.game.active;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.List;

public class FortressPlayer {
    public GameTeamKey team;
    public Text displayName;

    public long timeOfDeath;
    public long timeOfSpawn;

    public int kills;
    public int captures;
    public int deaths;

    public FortressPlayer(GameTeamKey team) {
        this.team = team;
    }

    public void giveModule(ServerPlayerEntity player, GameTeamKey team, ModuleItem item, int amount) {
        ItemStack moduleStack = new ItemStack(item, amount);
        var blockRegistry = player.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);

        BlockPredicate.Builder predicateBuilder = BlockPredicate.Builder.create();
        if (team == FortressTeams.RED.key()) {
            predicateBuilder.blocks(blockRegistry, Blocks.RED_CONCRETE, Blocks.RED_TERRACOTTA);
        } else {
            predicateBuilder.blocks(blockRegistry, Blocks.BLUE_CONCRETE, Blocks.BLUE_TERRACOTTA);
        }

        moduleStack.set(DataComponentTypes.CAN_PLACE_ON, new BlockPredicatesChecker(List.of(predicateBuilder.build()), false));

        player.getInventory().insertStack(moduleStack);
    }
}
