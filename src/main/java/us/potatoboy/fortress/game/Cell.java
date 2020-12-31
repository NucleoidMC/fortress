package us.potatoboy.fortress.game;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.fortress.game.active.CaptureManager;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Iterator;

public class Cell {
    private GameTeam owner;
    private final BlockPos center;
    public final BlockBounds bounds;
    private boolean occupied;

    public CaptureState captureState;
    public int captureTicks;

    public Cell(BlockPos center) {
        this.center = center;
        owner = null;
        occupied = false;
        bounds = new BlockBounds(center.add(-1, 0, -1), center.add(1, 0 , 1));
    }

    public GameTeam getOwner() {
        return owner;
    }

    public void setOwner(GameTeam owner, ServerWorld world, CellManager cellManager) {
        this.owner = owner;
        bounds.iterator().forEachRemaining(blockPos -> {
                    world.setBlockState(blockPos, cellManager.getTeamBlock(owner, blockPos));
                    world.setBlockState(blockPos.add(0, 20, 0), cellManager.getTeamGlass(owner));
        });
    }

    public BlockPos getCenter() {
        return center;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean incrementCapture(GameTeam team, ServerWorld world, int amount, CellManager cellManager) {
        captureTicks += amount;

        int captureIncrement = CaptureManager.CAPTURE_TICKS / 20 / 9;
        if ((captureTicks / 20) % captureIncrement == 0) {
            Iterator<BlockPos> iterator = bounds.iterator();
            for (int i = 0; i < (captureTicks / 20) / captureIncrement; i++) {
                if (iterator.hasNext()) {
                    BlockPos blockPos = iterator.next();

                    world.setBlockState(blockPos, cellManager.getTeamBlock(team, center));
                    world.setBlockState(blockPos.add(0, 20, 0), cellManager.getTeamGlass(team));
                }
            }
        }

        if (captureTicks >= CaptureManager.CAPTURE_TICKS) {
            captureTicks = 0;
            setOwner(team, world, cellManager);
            captureState = null;

            return true;
        }

        return false;
    }

    public boolean decrementCapture(ServerWorld world, int amount, CellManager cellManager) {
        captureTicks -= amount;

        int captureSec = captureTicks / 20;
        int captureIncrement = CaptureManager.CAPTURE_TICKS / 20 / 9;
        if (captureSec % captureIncrement == 0) {
            Iterator<BlockPos> iterator = BlockPos.iterate(bounds.getMax(), bounds.getMin()).iterator();
            int secureCount = ((CaptureManager.CAPTURE_TICKS / 20) - captureSec) / captureIncrement;
            BlockPos offset = center.add(1, 0, 1);
            for (int z = 0, i = 0; z > -3; z--) {
                for (int x = 0; x > -3 && i < secureCount; x--, i++) {
                    world.setBlockState(offset.add(x, 0 ,z), cellManager.getTeamBlock(owner, offset));
                }
            }
        }

        if (captureTicks <= 0) {
            captureTicks = 0;
            captureState = null;

            return true;
        }

        return false;
    }

    public void spawnParticles(ParticleEffect effect, ServerWorld world) {
        bounds.iterator().forEachRemaining(pos -> {
            world.spawnParticles(
                    effect,
                    pos.getX() + 0.5,
                    pos.getY() + 1,
                    pos.getZ() + 0.5,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        });
    }

    public void spawnTeamParticles(GameTeam team, ServerWorld world) {
        float[] colors = team.getDye().getColorComponents();
        DustParticleEffect effect = new DustParticleEffect(colors[0], colors[1], colors[2], 2);

        spawnParticles(effect, world);
    }
}
