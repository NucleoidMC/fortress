package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.random.Random;
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

    private static <T extends ModuleItem> T register(String path, Function<Item.Settings, T> function) {
        var id = Fortress.identifier(path);
        var item = function.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
        Registry.register(Registries.ITEM, id, item);
        return item;
    }

    public static ModuleItem getRandomModule(Random random) {
        return (ModuleItem) Registries.ITEM.getOrThrow(FortressItemTags.REGULAR_MODULES).getRandom(random).get().value();
    }

    public static ModuleItem getRandomSpecial(Random random) {
        return (ModuleItem) Registries.ITEM.getOrThrow(FortressItemTags.SPECIAL_MODULES).getRandom(random).get().value();
    }
}
