package us.potatoboy.fortress.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.active.CaptureManager;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cell {
    private GameTeam owner;
    private final BlockPos center;
    public final BlockBounds bounds;
    private List<ModuleItem> modules;
    public boolean enabled;

    public CaptureState captureState;
    public int captureTicks;

    public Cell(BlockPos center) {
        this.center = center;
        this.owner = null;
        this.modules = new ArrayList<>();
        this.bounds = new BlockBounds(center.add(-1, 0, -1), center.add(1, 0 , 1));
        this.enabled = true;
    }

    public GameTeam getOwner() {
        return owner;
    }

    public void setOwner(GameTeam owner, ServerWorld world, CellManager cellManager) {
        this.owner = owner;
        bounds.iterator().forEachRemaining(blockPos -> {
            world.setBlockState(blockPos, cellManager.getTeamBlock(owner, blockPos));
            setRoof(world, blockPos, cellManager, owner);
        });
    }

    public BlockPos getCenter() {
        return center;
    }

    public boolean hasModules() {
        return modules.isEmpty();
    }

    public boolean hasModuleAt(int index) {
        return modules.size() >= index + 1;
    }

    public void addModule(ModuleItem module) {
        modules.add(module);
    }

    public void tickModules(Object2ObjectMap<PlayerRef, FortressPlayer> participants, ServerWorld world) {
        modules.forEach(moduleItem -> moduleItem.tick(center, participants, owner, world));
    }

    public boolean incrementCapture(GameTeam team, ServerWorld world, int amount, CellManager cellManager) {
        captureTicks += amount;

        int captureIncrement = CaptureManager.CAPTURE_TICKS / 20 / 18;
        if ((captureTicks / 20) % captureIncrement == 0) {
            Iterator<BlockPos> iterator = bounds.iterator();
            for (int i = 0; i < (captureTicks / 20) / captureIncrement; i++) {
                if (iterator.hasNext()) {
                    BlockPos blockPos = iterator.next();

                    world.setBlockState(blockPos, cellManager.getTeamBlock(team, center));
                    setRoof(world, blockPos, cellManager, team);
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

    private void setRoof(ServerWorld world, BlockPos pos, CellManager cellManager, GameTeam team) {
        cellManager.roofHeight.ifPresent(integer -> world.setBlockState(pos.add(0, integer, 0), cellManager.getTeamGlass(team)));
    }

    public void setModuleColor(TeamPallet pallet, ServerWorld world) {
        BlockBounds moduleBounds = bounds.offset(0, 1, 0);
        moduleBounds = new BlockBounds(bounds.getMin(), bounds.getMax().add(0, modules.size() * 3, 0));

        moduleBounds.iterator().forEachRemaining(blockPos -> {
            BlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();

            if(block.isIn(BlockTags.PLANKS)) {
                world.setBlockState(blockPos, pallet.woodPlank.getDefaultState());
            } else if (block.isIn(BlockTags.WOODEN_STAIRS)) {
                world.setBlockState(blockPos, pallet.woodStair.getDefaultState()
                        .with(StairsBlock.FACING, state.get(StairsBlock.FACING))
                        .with(StairsBlock.HALF, state.get(StairsBlock.HALF))
                        .with(StairsBlock.SHAPE, state.get(StairsBlock.SHAPE))
                );
            } else if(block.isIn(BlockTags.WOODEN_SLABS)) {
                world.setBlockState(blockPos, pallet.woodSlab.getDefaultState()
                        .with(SlabBlock.TYPE, state.get(SlabBlock.TYPE))
                );
            }

            if (block == Blocks.RED_CONCRETE || block == Blocks.BLUE_CONCRETE) {
                world.setBlockState(blockPos, pallet.primary.getDefaultState());
            }
        });
    }

    public boolean decrementCapture(ServerWorld world, int amount, CellManager cellManager) {
        captureTicks -= amount;

        int captureSec = captureTicks / 20;
        int captureIncrement = CaptureManager.CAPTURE_TICKS / 20 / 18;
        if (captureSec % captureIncrement == 0) {
            Iterator<BlockPos> iterator = BlockPos.iterate(bounds.getMax(), bounds.getMin()).iterator();
            int secureCount = ((CaptureManager.CAPTURE_TICKS / 20) - captureSec) / captureIncrement;
            BlockPos offset = center.add(1, 0, 1);
            for (int z = 0, i = 0; z > -3; z--) {
                for (int x = 0; x > -3 && i < secureCount; x--, i++) {
                    world.setBlockState(offset.add(x, 0 ,z), cellManager.getTeamBlock(owner, offset));
                    setRoof(world, offset.add(x, 0, z), cellManager, owner);
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
