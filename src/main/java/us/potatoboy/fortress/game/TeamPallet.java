package us.potatoboy.fortress.game;

import net.minecraft.block.Block;

public record TeamPallet(Block primary, Block secondary,
                         Block glass, Block woodPlank,
                         Block woodStair, Block woodSlab) {
}
