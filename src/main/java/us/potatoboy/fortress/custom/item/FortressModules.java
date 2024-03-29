package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.Random;
import us.potatoboy.fortress.Fortress;


public class FortressModules {
    public static final ModuleItem CUBE = register("module_cube", new ModuleItem(Items.OAK_PLANKS, Fortress.identifier("cube")));
    public static final ModuleItem STAIRS = register("module_stairs", new ModuleItem(Items.OAK_STAIRS, Fortress.identifier("stairs")));
    public static final ModuleItem WALL = register("module_wall", new ModuleItem(Items.OAK_FENCE, Fortress.identifier("wall")));
    public static final ModuleItem INTERSECTION = register("module_intersection", new ModuleItem(Items.STRIPPED_OAK_WOOD, Fortress.identifier("intersection")));
    public static final ModuleItem DOOR = register("module_door", new ModuleItem(Items.OAK_DOOR, Fortress.identifier("door")));
    public static final ModuleItem BARRIER = register("module_barrier", new ModuleItem(Items.OAK_SLAB, Fortress.identifier("barrier")));
    public static final ModuleItem LAUNCH_PAD = register("module_launch_pad", new ModuleItem(Items.SLIME_BLOCK, Fortress.identifier("launcher")));
    public static final HealModuleItem HEAL = register("module_heal", new HealModuleItem(Fortress.identifier("heal")));
    public static final TeslaCoilModuleItem TESLA_COIL = register("module_tesla_coil", new TeslaCoilModuleItem(Fortress.identifier("tesla_coil")));

    private static <T extends ModuleItem> T register(String identifier, T item) {
        return Registry.register(Registries.ITEM, Fortress.identifier(identifier), item);
    }

    public static ModuleItem getRandomModule(Random random) {
        return (ModuleItem) Registries.ITEM.getEntryList(FortressItemTags.REGULAR_MODULES).get().getRandom(random).get().value();
    }

    public static ModuleItem getRandomSpecial(Random random) {
        return (ModuleItem) Registries.ITEM.getEntryList(FortressItemTags.SPECIAL_MODULES).get().getRandom(random).get().value();
    }
}
