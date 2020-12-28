package us.potatoboy.fortress.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import us.potatoboy.fortress.custom.FortressModules;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class FortressPlayer {
    public GameTeam team;

    public long timeOfDeath;
    public long timeOfSpawn;

    public int kills;

    public FortressPlayer(GameTeam team) {
        this.team = team;
    }

    public void giveKit(ServerPlayerEntity player, GameTeam team) {
        player.inventory.clear();

        ItemStack cube = new ItemStack(FortressModules.CUBE, 5);
        ItemStack stair = new ItemStack(FortressModules.STAIRS, 5);
        ItemStack wall = new ItemStack(FortressModules.WALL, 5);

        CompoundTag tag = new CompoundTag();

        ListTag canPlaceOn = new ListTag();
        canPlaceOn.add(StringTag.of(team == FortressTeams.RED ? "minecraft:red_concrete" : "minecraft:blue_concrete"));
        canPlaceOn.add(StringTag.of(team == FortressTeams.RED ? "minecraft:red_terracotta" : "minecraft:blue_terracotta"));
        tag.put("CanPlaceOn", canPlaceOn);

        cube.setTag(tag);
        stair.setTag(tag);
        wall.setTag(tag);

        player.inventory.insertStack(cube);
        player.inventory.insertStack(stair);
        player.inventory.insertStack(wall);
    }
}
