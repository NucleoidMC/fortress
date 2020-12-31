package us.potatoboy.fortress.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class FortressPlayer {
    public GameTeam team;

    public long timeOfDeath;
    public long timeOfSpawn;

    public int kills;
    public int captures;

    public FortressPlayer(GameTeam team) {
        this.team = team;
    }

    public void giveModule(ServerPlayerEntity player, GameTeam team, ModuleItem item, int amount) {
        ItemStack moduleStack = new ItemStack(item, amount);

        CompoundTag tag = new CompoundTag();

        ListTag canPlaceOn = new ListTag();
        canPlaceOn.add(StringTag.of(team == FortressTeams.RED ? "minecraft:red_concrete" : "minecraft:blue_concrete"));
        canPlaceOn.add(StringTag.of(team == FortressTeams.RED ? "minecraft:red_terracotta" : "minecraft:blue_terracotta"));
        tag.put("CanPlaceOn", canPlaceOn);

        moduleStack.setTag(tag);

        player.inventory.insertStack(moduleStack);
    }
}
