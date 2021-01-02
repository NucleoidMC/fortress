package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.world.GameMode;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.CaptureState;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.CellManager;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class CaptureManager {
    public static final int CAPTURE_TICKS = 20 * 9;

    private final GameSpace gameSpace;
    private final FortressActive game;

    private HashMap<GameTeam, HashSet<Integer>> capturedRows = new HashMap<>();

    CaptureManager(FortressActive game) {
        this.gameSpace = game.gameSpace;
        this.game = game;
    }

    public void tick(ServerWorld world, int interval) {
        HashMap<Cell, HashSet<ServerPlayerEntity>> cells = new HashMap<>();

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            ServerPlayerEntity player = entry.getKey().getEntity(world);
            if (player == null) continue;
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) continue;

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
        } else if (captureState == CaptureState.CONTESTED) {
            tickContested(cell, interval);
        }
    }

    private void tickContested(Cell cell, int interval) {
        ServerWorld world = gameSpace.getWorld();

        cell.spawnParticles(ParticleTypes.ANGRY_VILLAGER, world);
    }

    private void tickSecuring(Cell cell, int interval) {
        if (cell.decrementCapture(game.gameSpace.getWorld(), interval, game.getMap().cellManager)) {
            //secured
            cell.spawnTeamParticles(cell.getOwner(), gameSpace.getWorld());
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
            cell.spawnTeamParticles(captureTeam, world);

            CellManager cellManager = game.getMap().cellManager;
            int cellCollum = cellManager.getCellPos(cell.getCenter()).getLeft();
            if (cellManager.checkRow(cellCollum, captureTeam)) {
                capturedRows.putIfAbsent(captureTeam, new HashSet<>());

                if (!capturedRows.get(captureTeam).contains(cellCollum)) {
                    //Captured row for the first time
                    capturedRows.get(captureTeam).add(cellCollum);

                    for (Cell rowCell : cellManager.cells[cellCollum]) {
                        rowCell.spawnTeamParticles(captureTeam, world);
                    }

                    ServerPlayerEntity firstAttacker = attackers.iterator().next();
                    ModuleItem moduleItem = FortressModules.getRandomSpecial(firstAttacker.getRandom());
                    ItemStack stack = new ItemStack(moduleItem);
                    for (ServerPlayerEntity attacker : attackers) {
                        attacker.sendMessage(new LiteralText("New row captured!").formatted(captureTeam.getFormatting()), false);
                        attacker.sendMessage(
                                new TranslatableText("alert.fortress.give_module",
                                        stack.toHoverableText(), firstAttacker.getDisplayName()),
                                false);
                    }
                    game.getParticipant(firstAttacker).giveModule(firstAttacker, captureTeam, moduleItem, 1);
                }
            }

            for (ServerPlayerEntity attacker : attackers) {
                game.getParticipant(attacker).captures++;
            }
        } else {
            //capturing
        }
    }

    public void setRowCaptured(GameTeam team, int collum) {
        capturedRows.putIfAbsent(team, new HashSet<>());
        capturedRows.get(team).add(collum);
    }
}
