package us.potatoboy.fortress.custom.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.fake.FakeItem;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class ModuleItem extends Item implements FakeItem {
    private final Item proxy;
    public final Identifier structure;

    public ModuleItem(Item proxy, Identifier structure) {
        super(new Item.Settings());
        this.proxy = proxy;
        this.structure = structure;
    }

    @Override
    public Item asProxy() {
        return proxy;
    }

    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeam owner, ServerWorld world) {
        return;
    }
}
