package us.potatoboy.fortress.custom.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.packettweaker.PacketContext;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class ModuleItem extends Item implements PolymerItem {
    private final Item proxy;
    public final Identifier structureId;

    public ModuleItem(Item.Settings settings, Item proxy, Identifier structure) {
        super(settings);
        this.proxy = proxy;
        this.structureId = structure;
    }

    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerWorld world) {
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return proxy;
    }

    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    public StructureTemplate getStructure(MinecraftServer server) {
        return server.getStructureTemplateManager().getTemplate(structureId).orElseThrow();
    }
}
