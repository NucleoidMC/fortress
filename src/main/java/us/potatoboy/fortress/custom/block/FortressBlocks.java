package us.potatoboy.fortress.custom.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import us.potatoboy.fortress.Fortress;

import java.util.function.Function;

public class FortressBlocks {
    public static final Block LAUNCH_PAD = register("launch_pad", settings -> new LaunchPadBlock(settings.noCollision().noLootTable()));

    private static <T extends Block> T register(String path, Function<BlockBehaviour.Properties, T> function) {
        return register(path, BlockBehaviour.Properties.of(), function);
    }

    public static <T extends Block> T register(String path, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> function) {
        var id = Fortress.identifier(path);
        var item = function.apply(settings.setId(ResourceKey.create(Registries.BLOCK, id)));

        return Registry.register(BuiltInRegistries.BLOCK, id, item);
    }
}
