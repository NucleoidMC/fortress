package us.potatoboy.fortress.custom.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import us.potatoboy.fortress.Fortress;

import java.util.function.Function;

public class FortressBlocks {
    public static final Block LAUNCH_PAD = register("launch_pad", settings -> new LaunchPadBlock(settings.noCollision().dropsNothing()));

    private static <T extends Block> T register(String path, Function<AbstractBlock.Settings, T> function) {
        return register(path, AbstractBlock.Settings.create(), function);
    }

    public static <T extends Block> T register(String path, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> function) {
        var id = Fortress.identifier(path);
        var item = function.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));

        return Registry.register(Registries.BLOCK, id, item);
    }
}
