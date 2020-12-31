package us.potatoboy.fortress.custom.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import us.potatoboy.fortress.Fortress;
import us.potatoboy.fortress.custom.item.ModuleItem;

public class FortressBlocks {
    public static final Block LAUNCH_PAD = register("launch_pad", new LaunchPadBlock(Blocks.SLIME_BLOCK));

    private static <T extends Block> T register(String identifier, T block) {
        return Registry.register(Registry.BLOCK, Fortress.identifier(identifier), block);
    }
}
