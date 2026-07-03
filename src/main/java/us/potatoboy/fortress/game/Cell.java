package us.potatoboy.fortress.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cell {
    private GameTeamKey owner;
    private final BlockPos center;
    public final BlockBounds bounds;
    private final List<ModuleItem> modules;
    public boolean enabled;

    public CaptureState captureState;
    public int captureTicks;

    public Cell(BlockPos center) {
        this.center = center;
        this.owner = null;
        this.modules = new ArrayList<>();
        this.bounds = BlockBounds.of(center.offset(-1, 0, -1), center.offset(1, 0, 1));
        this.enabled = true;
    }

    public GameTeamKey getOwner() {
        return owner;
    }

    public void setOwner(GameTeamKey owner, ServerLevel level, CellManager cellManager) {
        this.owner = owner;
        bounds.iterator().forEachRemaining(blockPos -> level.setBlockAndUpdate(blockPos, cellManager.getTeamBlock(owner, blockPos)));
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

    public void tickModules(Object2ObjectMap<PlayerRef, FortressPlayer> participants, ServerLevel level) {
        modules.forEach(moduleItem -> moduleItem.tick(center, participants, owner, level));
    }

    public boolean incrementCapture(GameTeamKey team, ServerLevel level, int amount, CellManager cellManager) {
        captureTicks += amount;

        Iterator<BlockPos> iterator = bounds.iterator();
        for (int i = 0; i < captureTicks; i++) {
            if (iterator.hasNext()) {
                BlockPos blockPos = iterator.next();

                level.setBlockAndUpdate(blockPos, cellManager.getTeamBlock(team, center));
            }
        }

        if (captureTicks >= 9) {
            captureTicks = 0;
            setOwner(team, level, cellManager);
            captureState = null;

            return true;
        }

        return false;
    }

    public boolean decrementCapture(ServerLevel level, int amount, CellManager cellManager) {
        captureTicks -= amount;

        BlockPos offset = center.offset(1, 0, 1);
        for (int z = 0, i = 0; z > -3; z--) {
            for (int x = 0; x > -3 && i < 9 - captureTicks; x--, i++) {
                level.setBlockAndUpdate(offset.offset(x, 0, z), cellManager.getTeamBlock(owner, offset));
            }
        }

        if (captureTicks <= 0) {
            captureTicks = 0;
            captureState = null;

            return true;
        }

        return false;
    }

    public void setModuleColor(TeamPallet pallet, ServerLevel level) {
        BlockBounds moduleBounds = BlockBounds.of(bounds.min(), bounds.max().offset(0, modules.size() * 3, 0));
        
        moduleBounds.iterator().forEachRemaining(blockPos -> {
            BlockState state = level.getBlockState(blockPos);

            if (state.is(BlockTags.PLANKS)) {
                level.setBlockAndUpdate(blockPos, pallet.woodPlank().defaultBlockState());
            } else if (state.is(BlockTags.WOODEN_STAIRS)) {
                level.setBlockAndUpdate(blockPos, pallet.woodStair().defaultBlockState()
                        .setValue(StairBlock.FACING, state.getValue(StairBlock.FACING))
                        .setValue(StairBlock.HALF, state.getValue(StairBlock.HALF))
                        .setValue(StairBlock.SHAPE, state.getValue(StairBlock.SHAPE))
                );
            } else if (state.is(BlockTags.WOODEN_SLABS)) {
                level.setBlockAndUpdate(blockPos, pallet.woodSlab().defaultBlockState()
                        .setValue(SlabBlock.TYPE, state.getValue(SlabBlock.TYPE))
                );
            }

            Block block = state.getBlock();

            if (block == Blocks.CONCRETE.red() || block == Blocks.CONCRETE.blue()) {
                level.setBlockAndUpdate(blockPos, pallet.primary().defaultBlockState());
            }
        });
    }

    public void spawnParticles(ParticleOptions effect, ServerLevel level) {
        bounds.iterator().forEachRemaining(pos -> level.sendParticles(
                effect,
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                1,
                0.0, 0.0, 0.0,
                0.0
        ));
    }

    public void spawnTeamParticles(GameTeamConfig team, ServerLevel level) {
        int color = team.blockDyeColor().getFireworkColor();
        DustParticleOptions effect = new DustParticleOptions(color, 2);

        spawnParticles(effect, level);
    }
}
