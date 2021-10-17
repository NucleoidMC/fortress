package us.potatoboy.fortress.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;

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

        var canPlaceOn = new NbtList();
        canPlaceOn.add(NbtString.of(team == FortressTeams.RED.key() ? "minecraft:red_concrete" : "minecraft:blue_concrete"));
        canPlaceOn.add(NbtString.of(team == FortressTeams.RED.key() ? "minecraft:red_terracotta" : "minecraft:blue_terracotta"));

        moduleStack.getOrCreateNbt().put("CanPlaceOn", canPlaceOn);
        moduleStack.getOrCreateNbt().putInt("HideFlags", 63);

        player.getInventory().insertStack(moduleStack);
    }
}
