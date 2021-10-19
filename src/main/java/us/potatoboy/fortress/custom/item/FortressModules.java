package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import us.potatoboy.fortress.Fortress;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FortressModules {
    public static final List<ModuleItem> MODULES = new ArrayList<>();
    public static final List<ModuleItem> SPECIAL = new ArrayList<>();

    public static final ModuleItem CUBE = register("module_cube", new ModuleItem(Items.OAK_PLANKS, Fortress.identifier("cube")));
    public static final ModuleItem STAIRS = register("module_stairs", new ModuleItem(Items.OAK_STAIRS, Fortress.identifier("stairs")));
    public static final ModuleItem WALL = register("module_wall", new ModuleItem(Items.OAK_FENCE, Fortress.identifier("wall")));
    public static final ModuleItem INTERSECTION = register("module_intersection", new ModuleItem(Items.STRIPPED_OAK_WOOD, Fortress.identifier("intersection")));
    public static final ModuleItem DOOR = register("module_door", new ModuleItem(Items.OAK_DOOR, Fortress.identifier("door")));
    public static final ModuleItem BARRIER = register("module_barrier", new ModuleItem(Items.OAK_SLAB, Fortress.identifier("barrier")));

    public static final ModuleItem LAUNCH_PAD = registerSpecial("module_launch_pad", new ModuleItem(Items.SLIME_BLOCK, Fortress.identifier("launcher")));
    public static final HealModuleItem HEAL = registerSpecial("module_heal", new HealModuleItem(Fortress.identifier("heal")));

    private static <T extends ModuleItem> T register(String identifier, T item) {
        MODULES.add(item);
        return Registry.register(Registry.ITEM, Fortress.identifier(identifier), item);
    }

    private static <T extends ModuleItem> T registerSpecial(String identifier, T item) {
        SPECIAL.add(item);
        return Registry.register(Registry.ITEM, Fortress.identifier(identifier), item);
    }

    public static ModuleItem getRandomModule(Random random) {
        int num = random.nextInt(FortressModules.MODULES.size());
        return FortressModules.MODULES.get(num);
    }

    public static ModuleItem getRandomSpecial(Random random) {
        int num = random.nextInt(FortressModules.SPECIAL.size());
        return FortressModules.SPECIAL.get(num);
    }
}
