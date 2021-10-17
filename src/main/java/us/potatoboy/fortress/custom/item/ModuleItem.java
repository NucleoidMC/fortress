package us.potatoboy.fortress.custom.item;

import eu.pb4.polymer.item.VirtualItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class ModuleItem extends Item implements VirtualItem {
    private final Item proxy;
    public final Identifier structure;

    public ModuleItem(Item proxy, Identifier structure) {
        super(new Item.Settings());
        this.proxy = proxy;
        this.structure = structure;
    }

    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerWorld world) {
    }

    @Override
    public Item getVirtualItem() {
        return proxy;
    }
}
