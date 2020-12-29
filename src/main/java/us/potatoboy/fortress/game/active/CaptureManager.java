package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import us.potatoboy.fortress.game.CaptureState;
import us.potatoboy.fortress.game.Cell;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.HashMap;
import java.util.HashSet;

public class CaptureManager {
    public static final int CAPTURE_TICKS = 20 * 9;

    private final GameSpace gameSpace;
    private final FortressActive game;

    CaptureManager(FortressActive game) {
        this.gameSpace = game.gameSpace;
        this.game = game;
    }

    public void tick(ServerWorld world, int interval) {
        HashMap<Cell, HashSet<ServerPlayerEntity>> cells = new HashMap<>();

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            ServerPlayerEntity player = entry.getKey().getEntity(world);
            if (player == null) continue;

            FortressPlayer participant = entry.getValue();

            Cell currentCell = game.getMap().cellManager.getCell(player.getBlockPos());

            if (currentCell == null) continue;

            if (currentCell.getOwner() == null) {
                boolean ownsNeighbor = false;

                Pair<Integer, Integer> location = game.getMap().cellManager.getCellPos(player.getBlockPos());
                Cell[][] mapCells = game.getMap().cellManager.cells;
                if (location == null) continue;

                for (int x = location.getLeft() - 1; x <= (location.getLeft()) + 1; x += 1 ) {
                    for (int z = location.getRight() - 1; z <= (location.getRight() + 1); z += 1) {
                        if (x < 0 || z < 0 || x >= mapCells.length || z >= mapCells[x].length) {
                            continue;
                        }

                        Cell neighborCell = game.getMap().cellManager.cells[x][z];
                        if (neighborCell.getOwner() == participant.team) {
                            ownsNeighbor = true;
                        }
                    }
                }

                if (!ownsNeighbor) continue;
            }

            cells.putIfAbsent(currentCell, new HashSet<>());
            cells.get(currentCell).add(player);
        }

        for (Cell cell : cells.keySet()) {
            tickCell(cell, cells.get(cell), interval);
        }
    }

    private void tickCell(Cell cell, HashSet<ServerPlayerEntity> players, int interval) {
        HashSet<ServerPlayerEntity> defenders = new HashSet<>();
        HashSet<ServerPlayerEntity> attackers = new HashSet<>();

        for (ServerPlayerEntity player : players) {
            FortressPlayer participant = game.getParticipant(player);

            if (cell.getOwner() == participant.team) {
                defenders.add(player);
            } else {
                attackers.add(player);
            }
        }

        boolean defendersSecuring = !defenders.isEmpty();
        boolean attackersCapturing = !attackers.isEmpty();
        boolean contested = defendersSecuring && attackersCapturing;

        CaptureState captureState = null;

        if (attackersCapturing) {
            if (!contested) {
                captureState = CaptureState.CAPTURING;
            } else {
                captureState = CaptureState.CONTESTED;
            }
        } else {
            if (cell.captureTicks > 0) {
                captureState = CaptureState.SECURING;
            }
        }

        cell.captureState = captureState;

        if (captureState == CaptureState.CAPTURING) {
            tickCapturing(cell, interval, attackers);
        } else if (captureState == CaptureState.SECURING) {
            tickSecuring(cell, interval);
        }
    }

    private void tickSecuring(Cell cell, int interval) {
        if (cell.decrementCapture(game.gameSpace.getWorld(), interval, game.getMap().cellManager)) {
            //secured
        }
    }

    private void tickCapturing(Cell cell, int interval, HashSet<ServerPlayerEntity> attackers) {
        if (cell.captureTicks == 0) {
            //began capturing
        }

        GameTeam captureTeam = game.getParticipant(attackers.iterator().next()).team;
        ServerWorld world = gameSpace.getWorld();

        if (cell.incrementCapture(captureTeam, world, interval * attackers.size(), game.getMap().cellManager)) {
            //captured
            cell.bounds.iterator().forEachRemaining(blockPos ->
                    game.gameSpace.getWorld().setBlockState(blockPos, game.getMap().cellManager.getTeamBlock(captureTeam, blockPos)
                    ));
        } else {
            //capturing
        }
    }
}
