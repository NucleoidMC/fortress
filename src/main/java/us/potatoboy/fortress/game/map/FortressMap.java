package us.potatoboy.fortress.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.CellManager;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.List;

public class FortressMap {
    private final MapTemplate template;

    public final BlockBounds bounds;
    public final List<BlockBounds> redSpawns = new ArrayList<>();
    public final List<BlockBounds> blueSpawns = new ArrayList<>();
    public BlockBounds waitingSpawn = BlockBounds.ofBlock(BlockPos.ORIGIN);
    public final CellManager cellManager;

    public FortressMap(MapTemplate template, CellManager cellManager) {
        this.template = template;
        this.bounds = template.getBounds();
        this.cellManager = cellManager;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public BlockBounds getSpawn(GameTeamKey team, Random random) {
        if (team == FortressTeams.RED.key()) {
            return redSpawns.get(random.nextInt(redSpawns.size()));
        } else {
            return blueSpawns.get(random.nextInt(blueSpawns.size()));
        }
    }

    public Pair<Integer, Integer> getControlPercent() {
        Cell[][] rows = cellManager.cells;

        int redCells = 0;
        int blueCells = 0;
        int disabledCells = 0;

        for (Cell[] row : rows) {
            for (Cell cell : row) {
                if (!cell.enabled) {
                    disabledCells++;
                    continue;
                }

                if (cell.getOwner() == FortressTeams.RED.key()) {
                    redCells++;
                } else if (cell.getOwner() == FortressTeams.BLUE.key()) {
                    blueCells++;
                }
            }
        }

        float size = (float) Math.pow(rows.length, 2) - disabledCells;
        int redPercent = Math.round(((float) redCells / size) * 100);
        int bluePercent = Math.round(((float) blueCells / size) * 100);

        return new Pair<>(redPercent, bluePercent);
    }

    public void setStarterCells(GameTeam team, String region, ServerWorld world) {
        template.getMetadata().getRegionBounds(region).forEach(bounds -> cellManager.setCellsOwner(bounds, team.key(), world));
    }
}
