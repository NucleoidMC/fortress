package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.*;

public class FortressKit {
    private final LinkedHashMap<ModuleItem, Integer> starterModules = new LinkedHashMap<>();
    private final LinkedHashMap<Item, Integer> starterItems = new LinkedHashMap<>();

    private ServerLevel level;
    private FortressTeams teams;

    FortressKit(ServerLevel level, FortressTeams teams) {
        this.level = level;
        this.teams = teams;

        starterItems.put(Items.STONE_SWORD, 1);
        starterItems.put(Items.WOODEN_AXE, 1);
        starterItems.put(Items.WOODEN_PICKAXE, 1);
        starterItems.put(Items.SHIELD, 1);
        starterItems.put(Items.BOW, 1);
        starterItems.put(Items.ARROW, 1);

        starterModules.put(FortressModules.CUBE, 2);
        starterModules.put(FortressModules.WALL, 3);
        starterModules.put(FortressModules.STAIRS, 3);
        starterModules.put(FortressModules.BARRIER, 2);
    }

    public void giveStarterKit(Object2ObjectMap<PlayerRef, FortressPlayer> participants) {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(level, playerEntity -> playerEntity.getInventory().clearContent());
            entry.getKey().ifOnline(level, playerEntity -> giveItems(playerEntity, entry.getValue().team));
        }

        HashMap<PlayerRef, FortressPlayer> redTeam = new HashMap<>();
        HashMap<PlayerRef, FortressPlayer> blueTeam = new HashMap<>();

        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            if (entry.getValue().team == FortressTeams.RED.key()) {
                redTeam.put(entry.getKey(), entry.getValue());
            } else {
                blueTeam.put(entry.getKey(), entry.getValue());
            }
        }

        giveModules(redTeam, FortressTeams.RED.key());
        giveModules(blueTeam, FortressTeams.BLUE.key());

        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(level, playerEntity -> playerEntity.inventoryMenu.broadcastChanges());
        }
    }

    private void giveArmor(ServerPlayer playerEntity, GameTeamKey team) {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        var registry = playerEntity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(
                boots.get(DataComponents.ENCHANTMENTS)
        );
        builder.upgrade(registry.getOrThrow(Enchantments.FEATHER_FALLING), 5);
        boots.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
        boots.set(DataComponents.DYED_COLOR, new DyedItemColor(teams.getConfig(team).dyeColor().getValue()));

        playerEntity.setItemSlot(EquipmentSlot.FEET, boots);
    }

    public void giveItems(ServerPlayer playerEntity, GameTeamKey team) {
        for (Map.Entry<Item, Integer> entry : starterItems.entrySet()) {
            ItemStack itemStack = new ItemStack(entry.getKey(), entry.getValue());

            if (entry.getKey() instanceof ShieldItem) {
                var registry = playerEntity.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
                var bannerPattern = new BannerPatternLayers.Builder()
                        .addIfRegistered(registry, BannerPatterns.BASE, teams.getConfig(team).blockDyeColor())
                        .build();
                itemStack.set(DataComponents.BANNER_PATTERNS, bannerPattern);
            }

            if (entry.getKey() instanceof BowItem) {
                var registry = playerEntity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(
                        itemStack.get(DataComponents.ENCHANTMENTS)
                );
                builder.upgrade(registry.getOrThrow(Enchantments.INFINITY), 1);
                itemStack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
            }

            if (entry.getKey().builtInRegistryHolder().is(ItemTags.PICKAXES)) {
                itemStack.set(DataComponents.CAN_BREAK, new AdventureModePredicate(List.of(
                        BlockPredicate.Builder.block()
                                .of(level.registryAccess().lookupOrThrow(Registries.BLOCK), BlockTags.PLANKS)
                                .build()
                )));
            }

            itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

            if (entry.getKey() instanceof ShieldItem) {
                playerEntity.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            } else {
                playerEntity.getInventory().add(itemStack);
            }
        }

        giveArmor(playerEntity, team);
    }

    private void giveModules(HashMap<PlayerRef, FortressPlayer> players, GameTeamKey team) {
        Iterator<Map.Entry<PlayerRef, FortressPlayer>> playerItr = players.entrySet().iterator();

        for (Map.Entry<ModuleItem, Integer> entry : starterModules.entrySet()) {
            int num = entry.getValue();

            for (int i = 0; i < num; i++) {
                if (!playerItr.hasNext()) {
                    playerItr = players.entrySet().iterator();
                }

                Map.Entry<PlayerRef, FortressPlayer> playerEntry = playerItr.next();
                FortressPlayer participant = playerEntry.getValue();
                playerEntry.getKey().ifOnline(level, playerEntity -> {
                    participant.giveModule(playerEntity, team, entry.getKey(), 1);
                });
            }
        }
    }
}
