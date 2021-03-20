package us.potatoboy.fortress.game;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Optional;

public class CellManager {
    public final Cell[][] cells;
    public final BlockBounds bounds;
    public Optional<Integer> roofHeight = Optional.empty();

    public CellManager(BlockBounds bounds) {
        BlockPos max = bounds.getMax();
        BlockPos min = bounds.getMin();
        BlockPos size = bounds.getSize();

        int length = size.getX() + 1;
        int width = size.getZ() + 1;

        if (length % 3 != 0 || width % 3 != 0) {
            throw new GameOpenException(new LiteralText("Map cells not divisible by 3"));
        }

        cells = new Cell[length / 3][width / 3];

        BlockPos offset = min.add(1, 0, 1);
        for (int z = 0, i = 0; z < length; z += 3, i++) {
            for (int x = 0, i2 = 0; x < width; x += 3, i2++) {
                cells[i][i2] = new Cell(offset.add(x, 0, z));
            }
        }

        this.bounds = bounds;
    }

    public void disableCells(BlockBounds bounds) {
        BlockPos pos = new BlockPos(bounds.getMax().getX(), bounds.getMin().getY(), bounds.getMax().getZ());
        BlockBounds layerBounds = new BlockBounds(bounds.getMin(), pos);

        layerBounds.iterator().forEachRemaining(blockPos -> {
            getCell(blockPos).enabled = false;
        });
    }

    public void setCellsOwner(BlockBounds bounds, GameTeam team, ServerWorld world) {
        BlockPos pos = new BlockPos(bounds.getMax().getX(), bounds.getMin().getY(), bounds.getMax().getZ());
        BlockBounds layerBounds = new BlockBounds(bounds.getMin(), pos);

        layerBounds.iterator().forEachRemaining(blockPos -> {
            getCell(blockPos).setOwner(team, world, this);
        });
    }

    public Cell getCell(BlockPos blockPos) {
        Pair<Integer, Integer> location = getCellPos(blockPos);
        if (location == null) return null;

        return cells[location.getLeft()][location.getRight()];
    }

    public Pair<Integer, Integer> getCellPos(BlockPos blockPos) {
        if (!bounds.contains(blockPos.getX(), blockPos.getZ())) return null;

        BlockPos offsetPos = blockPos.subtract(bounds.getMin());

        int cellX = offsetPos.getX();
        int cellZ = offsetPos.getZ();

        cellX = round(cellX - 1, 3)/3;
        cellZ = round(cellZ - 1, 3)/3;

        return new Pair<>(cellZ, cellX);
    }

    public int round(int value, int multiplier) {
        return (value + multiplier / 2) / multiplier * multiplier;
    }

    public BlockState getTeamGlass(GameTeam team) {
        if (team == FortressTeams.RED) {
            return Blocks.RED_STAINED_GLASS.getDefaultState();
        } else if (team == FortressTeams.BLUE) {
            return Blocks.BLUE_STAINED_GLASS.getDefaultState();
        }

        return Blocks.LIGHT_GRAY_STAINED_GLASS.getDefaultState();
    }

    public BlockState getTeamBlock(GameTeam team, BlockPos pos) {
        Pair<Integer, Integer> location = getCellPos(pos);

        boolean primary = (location.getLeft() + location.getRight()) % 2 != 0;

        TeamPallet pallet = team == FortressTeams.RED ? FortressTeams.RED_PALLET : FortressTeams.BLUE_PALLET;

        ItemStack itemStack = new ItemStack(primary ? pallet.primary : pallet.secondary);

        return ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
    }

    public boolean checkRow(int collum, GameTeam team) {
        for (Cell cell : cells[collum]) {
            if (cell.getOwner() != team) {
                return false;
            }
        }

        return true;
    }

    public int getFloorHeight() {
        return bounds.getMax().getY();
    }
}
