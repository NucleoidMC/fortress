package us.potatoboy.fortress.custom.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class ModuleItem extends Item implements PolymerItem {
    private final Item proxy;
    public final Identifier structureId;

    public ModuleItem(Item.Properties settings, Item proxy, Identifier structure) {
        super(settings);
        this.proxy = proxy;
        this.structureId = structure;
    }

    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerLevel level) {
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return proxy;
    }

    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return null;
    }

    public StructureTemplate getStructure(MinecraftServer server) {
        return server.getStructureManager().get(structureId).orElseThrow();
    }
}
