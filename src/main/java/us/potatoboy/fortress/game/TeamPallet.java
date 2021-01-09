package us.potatoboy.fortress.game;

import net.minecraft.block.Block;

public class TeamPallet {
    public final Block primary;
    public final Block secondary;
    public final Block glass;
    public final Block woodPlank;
    public final Block woodStair;
    public final Block woodSlab;

    public TeamPallet(Block primary, Block secondary, Block glass, Block woodPlank, Block woodStair, Block woodSlab) {
        this.primary = primary;
        this.secondary = secondary;
        this.glass = glass;
        this.woodPlank = woodPlank;
        this.woodStair = woodStair;
        this.woodSlab = woodSlab;
    }
}
