package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FortressKit {
    private final LinkedHashMap<ModuleItem, Integer> starterModules = new LinkedHashMap<>();
    private final LinkedHashMap<Item, Integer> starterItems = new LinkedHashMap<>();

    private ServerWorld world;

    FortressKit (ServerWorld world) {
        this.world = world;

        starterItems.put(Items.STONE_SWORD, 1);
        starterItems.put(Items.WOODEN_AXE, 1);
        starterItems.put(Items.SHIELD, 1);

        starterModules.put(FortressModules.CUBE, 2);
        starterModules.put(FortressModules.WALL, 3);
        starterModules.put(FortressModules.STAIRS, 3);
        starterModules.put(FortressModules.BARRIER, 2);
    }

    public void giveStarterKit(Object2ObjectMap<PlayerRef, FortressPlayer> participants) {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(world, playerEntity -> playerEntity.inventory.clear());
            entry.getKey().ifOnline(world, playerEntity -> giveItems(playerEntity, entry.getValue().team));
        }

        HashMap<PlayerRef, FortressPlayer> redTeam = new HashMap<>();
        HashMap<PlayerRef, FortressPlayer> blueTeam = new HashMap<>();

        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            if (entry.getValue().team == FortressTeams.RED) {
                redTeam.put(entry.getKey(), entry.getValue());
            } else {
                blueTeam.put(entry.getKey(), entry.getValue());
            }
        }

        giveModules(redTeam, FortressTeams.RED);
        giveModules(blueTeam, FortressTeams.BLUE);
    }

    public void giveItems(ServerPlayerEntity playerEntity, GameTeam team) {
        for (Map.Entry<Item, Integer> entry : starterItems.entrySet()) {
            ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());

            if (entry.getKey() instanceof ShieldItem) {
                CompoundTag tag2 = new CompoundTag();
                tag2.putInt("Base", team == FortressTeams.RED ? 14 : 11);
                itemStack.putSubTag("BlockEntityTag", tag2);
            }

            itemStack.getOrCreateTag().putBoolean("Unbreakable", true);
            itemStack.getOrCreateTag().putInt("HideFlags", 63);

            playerEntity.inventory.insertStack(itemStack);
        }
    }

    private void giveModules(HashMap<PlayerRef, FortressPlayer> players, GameTeam team) {
        Iterator<Map.Entry<PlayerRef, FortressPlayer>> playerItr = players.entrySet().iterator();

        for (Map.Entry<ModuleItem, Integer> entry : starterModules.entrySet()) {
            int num = entry.getValue();

            for (int i = 0; i < num; i++) {
                if (!playerItr.hasNext()) {
                    playerItr = players.entrySet().iterator();
                }

                Map.Entry<PlayerRef, FortressPlayer> playerEntry = playerItr.next();
                FortressPlayer participant = playerEntry.getValue();
                playerEntry.getKey().ifOnline(world, playerEntity -> {
                    participant.giveModule(playerEntity, team, entry.getKey(), 1);
                });
            }
        }
    }
}
