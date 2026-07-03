package us.potatoboy.fortress.custom.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import us.potatoboy.fortress.Fortress;

import java.util.function.Function;


public class FortressModules {
    public static final ModuleItem CUBE = register("module_cube", settings -> new ModuleItem(settings, Items.OAK_PLANKS, Fortress.identifier("cube")));
    public static final ModuleItem STAIRS = register("module_stairs", settings -> new ModuleItem(settings, Items.OAK_STAIRS, Fortress.identifier("stairs")));
    public static final ModuleItem WALL = register("module_wall", settings -> new ModuleItem(settings, Items.OAK_FENCE, Fortress.identifier("wall")));
    public static final ModuleItem INTERSECTION = register("module_intersection", settings -> new ModuleItem(settings, Items.STRIPPED_OAK_WOOD, Fortress.identifier("intersection")));
    public static final ModuleItem DOOR = register("module_door", settings -> new ModuleItem(settings, Items.OAK_DOOR, Fortress.identifier("door")));
    public static final ModuleItem BARRIER = register("module_barrier", settings -> new ModuleItem(settings, Items.OAK_SLAB, Fortress.identifier("barrier")));
    public static final ModuleItem LAUNCH_PAD = register("module_launch_pad", settings -> new ModuleItem(settings, Items.SLIME_BLOCK, Fortress.identifier("launcher")));
    public static final HealModuleItem HEAL = register("module_heal", settings -> new HealModuleItem(settings, Fortress.identifier("heal")));
    public static final TeslaCoilModuleItem TESLA_COIL = register("module_tesla_coil", settings -> new TeslaCoilModuleItem(settings, Fortress.identifier("tesla_coil")));

    private static <T extends ModuleItem> T register(String path, Function<Item.Properties, T> function) {
        var id = Fortress.identifier(path);
        var item = function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    public static ModuleItem getRandomModule(RandomSource random) {
        return (ModuleItem) BuiltInRegistries.ITEM.getOrThrow(FortressItemTags.REGULAR_MODULES).getRandomElement(random).get().value();
    }

    public static ModuleItem getRandomSpecial(RandomSource random) {
        return (ModuleItem) BuiltInRegistries.ITEM.getOrThrow(FortressItemTags.SPECIAL_MODULES).getRandomElement(random).get().value();
    }
}
