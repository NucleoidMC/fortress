package us.potatoboy.fortress.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import us.potatoboy.fortress.game.ModuleManager;
import xyz.nucleoid.plasmid.fake.FakeItem;

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
}
