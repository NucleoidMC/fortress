package us.potatoboy.fortress.game;

import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import us.potatoboy.fortress.custom.FortressModules;
import us.potatoboy.fortress.custom.ModuleItem;

import java.util.HashMap;

public class ModuleManager {
    private final HashMap<ModuleItem, Structure> structures = new HashMap<>();

    public ModuleManager (StructureManager structureManager) {
        for (ModuleItem moduleItem : FortressModules.MODULES) {
            structures.put(moduleItem, structureManager.getStructure(moduleItem.structure));
        }
    }

    public Structure getStructure(ModuleItem item) {
        return structures.get(item);
    }
}
