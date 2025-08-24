package us.potatoboy.fortress.game;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

public record TeamPallet(
        Block primary,
        Block secondary,
        Block glass,
        Block woodPlank,
        Block woodStair,
        Block woodSlab
) {
    public static TeamPallet of(DyeColor color, Block woodPlank, Block woodStair, Block woodSlab) {
        return new TeamPallet(
                ColoredBlocks.concrete(color),
                ColoredBlocks.terracotta(color),
                ColoredBlocks.glass(color),
                woodPlank,
                woodStair,
                woodSlab
        );
    }

    public static TeamPallet of(DyeColor color) {
        return of(color,
                switch (color) {
                    case WHITE, LIGHT_GRAY -> Blocks.PALE_OAK_PLANKS;
                    case ORANGE -> Blocks.ACACIA_PLANKS;
                    case MAGENTA, PURPLE, RED -> Blocks.CRIMSON_PLANKS;
                    case LIGHT_BLUE, LIME, BLUE, CYAN, GREEN -> Blocks.WARPED_PLANKS;
                    case YELLOW -> Blocks.BIRCH_PLANKS;
                    case PINK -> Blocks.CHERRY_PLANKS;
                    case GRAY, BLACK -> Blocks.DARK_OAK_PLANKS;
                    case BROWN -> Blocks.SPRUCE_PLANKS;
                },
                switch (color) {
                    case WHITE, LIGHT_GRAY -> Blocks.PALE_OAK_STAIRS;
                    case ORANGE -> Blocks.ACACIA_STAIRS;
                    case MAGENTA, PURPLE, RED -> Blocks.CRIMSON_STAIRS;
                    case LIGHT_BLUE, LIME, BLUE, CYAN, GREEN -> Blocks.WARPED_STAIRS;
                    case YELLOW -> Blocks.BIRCH_STAIRS;
                    case PINK -> Blocks.CHERRY_STAIRS;
                    case GRAY, BLACK -> Blocks.DARK_OAK_STAIRS;
                    case BROWN -> Blocks.SPRUCE_STAIRS;
                },
                switch (color) {
                    case WHITE, LIGHT_GRAY -> Blocks.PALE_OAK_SLAB;
                    case ORANGE -> Blocks.ACACIA_SLAB;
                    case MAGENTA, PURPLE, RED -> Blocks.CRIMSON_SLAB;
                    case LIGHT_BLUE, LIME, BLUE, CYAN, GREEN -> Blocks.WARPED_SLAB;
                    case YELLOW -> Blocks.BIRCH_SLAB;
                    case PINK -> Blocks.CHERRY_SLAB;
                    case GRAY, BLACK -> Blocks.DARK_OAK_SLAB;
                    case BROWN -> Blocks.SPRUCE_SLAB;
                }
        );
    }
}
