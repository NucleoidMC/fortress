package us.potatoboy.fortress.custom.item;

import eu.pb4.polymer.api.item.PolymerItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class ModuleItem extends Item implements PolymerItem {
    private final Item proxy;
    public final Identifier structureId;

    public ModuleItem(Item proxy, Identifier structure) {
        super(new Item.Settings());
        this.proxy = proxy;
        this.structureId = structure;
    }

    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerWorld world) {
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return proxy;
    }

    public Structure getStructure(MinecraftServer server) {
        return server.getStructureManager().getStructure(structureId).orElseThrow();
    }
}
