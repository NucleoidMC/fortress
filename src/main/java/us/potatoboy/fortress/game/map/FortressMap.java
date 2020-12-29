package us.potatoboy.fortress.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.CellManager;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FortressMap {
    private final MapTemplate template;

    public final BlockBounds bounds;
    public final List<BlockBounds> redSpawns = new ArrayList<>();
    public final List<BlockBounds> blueSpawns = new ArrayList<>();
    public BlockBounds waitingSpawn = BlockBounds.EMPTY;
    public final CellManager cellManager;

    public FortressMap(MapTemplate template, CellManager cellManager) {
        this.template = template;
        this.bounds = template.getBounds();
        this.cellManager = cellManager;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public BlockBounds getSpawn(GameTeam team, Random random) {
        if (team == FortressTeams.RED) {
            return redSpawns.get(random.nextInt(redSpawns.size()));
        } else {
            return blueSpawns.get(random.nextInt(blueSpawns.size()));
        }
    }

    public Pair getControlPercent() {
        Cell[][] cells = cellManager.cells;

        int redCells = 0;
        int blueCells = 0;

        for (int z = 0; z < cells.length; z++) {
            for (int x = 0; x < cells[z].length; x++) {
                if (cells[z][x].getOwner() == FortressTeams.RED) {
                    redCells++;
                } else if (cells[z][x].getOwner() == FortressTeams.BLUE) {
                    blueCells++;
                }
            }
        }

        float size = (float) Math.pow(cells.length, 2);
        int redPercent = Math.round(((float)redCells / size) * 100);
        int bluePercent = Math.round(((float)blueCells / size) * 100);

        return new Pair(redPercent, bluePercent);
    }
}