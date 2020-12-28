package us.potatoboy.fortress.custom;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import us.potatoboy.fortress.Fortress;

import java.util.HashSet;

public class FortressModules {
    public static final HashSet<ModuleItem> MODULES = new HashSet<>();

    public static final Item CUBE = register("module_cube", new ModuleItem(Items.OAK_PLANKS, Fortress.identifier("cube")));
    public static final Item STAIRS = register("module_stairs", new ModuleItem(Items.OAK_STAIRS, Fortress.identifier("stairs")));
    public static final Item WALL = register("module_wall", new ModuleItem(Items.OAK_FENCE, Fortress.identifier("wall")));

    private static <T extends ModuleItem> T register(String identifier, T item) {
        MODULES.add(item);
        return Registry.register(Registry.ITEM, Fortress.identifier(identifier), item);
    }
}
