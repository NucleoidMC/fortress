package us.potatoboy.fortress.custom.block;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import us.potatoboy.fortress.Fortress;

public class FortressBlocks {
    public static final Block LAUNCH_PAD = register("launch_pad", new LaunchPadBlock());

    private static <T extends Block> T register(String identifier, T block) {
        return Registry.register(Registries.BLOCK, Fortress.identifier(identifier), block);
    }
}
