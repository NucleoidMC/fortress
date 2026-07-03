package us.potatoboy.fortress.game.active;

import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.List;

public class FortressPlayer {
    public GameTeamKey team;
    public Component displayName;

    public long timeOfDeath;
    public long timeOfSpawn;

    public int kills;
    public int captures;
    public int deaths;

    public FortressPlayer(GameTeamKey team) {
        this.team = team;
    }

    public void giveModule(ServerPlayer player, GameTeamKey team, ModuleItem item, int amount) {
        ItemStack moduleStack = new ItemStack(item, amount);
        var blockRegistry = player.registryAccess().lookupOrThrow(Registries.BLOCK);

        BlockPredicate.Builder predicateBuilder = BlockPredicate.Builder.block();
        if (team == FortressTeams.RED.key()) {
            predicateBuilder.of(blockRegistry, Blocks.CONCRETE.red(), Blocks.DYED_TERRACOTTA.red());
        } else {
            predicateBuilder.of(blockRegistry, Blocks.CONCRETE.blue(), Blocks.DYED_TERRACOTTA.blue());
        }

        moduleStack.set(DataComponents.CAN_PLACE_ON, new AdventureModePredicate(List.of(predicateBuilder.build())));

        player.getInventory().add(moduleStack);
    }
}
