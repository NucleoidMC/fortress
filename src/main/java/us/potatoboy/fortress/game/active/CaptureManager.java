package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.world.GameMode;
import us.potatoboy.fortress.Fortress;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.CaptureState;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.CellManager;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.HashMap;
import java.util.HashSet;

public class CaptureManager {
    public static final int CAPTURE_TICK_DELAY = 10;

    private final GameSpace gameSpace;
    private final FortressActive game;

    private HashMap<GameTeam, HashSet<Integer>> capturedRows = new HashMap<>();

    CaptureManager(FortressActive game) {
        this.gameSpace = game.gameSpace;
        this.game = game;
    }

    public void tick(ServerWorld world) {
        HashMap<Cell, HashSet<ServerPlayerEntity>> cells = new HashMap<>();

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            ServerPlayerEntity player = entry.getKey().getEntity(world);
            if (player == null) continue;
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) continue;

            FortressPlayer participant = entry.getValue();

            Cell currentCell = game.getMap().cellManager.getCell(player.getBlockPos());

            if (currentCell == null) continue;
            if (!currentCell.enabled) continue;

            if (!game.config.recapture && currentCell.getOwner() != null) continue;

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

            if (game.config.captureEnemy) {
                if (currentCell.getOwner() == null) {
                    if (!ownsNeighbor) {
                        continue;
                    }
                }
            } else {
                if (!ownsNeighbor) continue;
            }

            cells.putIfAbsent(currentCell, new HashSet<>());
            cells.get(currentCell).add(player);
        }

        for (Cell cell : cells.keySet()) {
            tickCell(cell, cells.get(cell));
        }
    }

    private void tickCell(Cell cell, HashSet<ServerPlayerEntity> players) {
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
            tickCapturing(cell, attackers);
        } else if (captureState == CaptureState.SECURING) {
            tickSecuring(cell, defenders);
        } else if (captureState == CaptureState.CONTESTED) {
            tickContested(cell);
        }
    }

    private void tickContested(Cell cell) {
        ServerWorld world = gameSpace.getWorld();

        cell.spawnParticles(ParticleTypes.ANGRY_VILLAGER, world);
    }

    private void tickSecuring(Cell cell, HashSet<ServerPlayerEntity> defenders) {
        if (cell.decrementCapture(game.gameSpace.getWorld(), defenders.size(), game.getMap().cellManager)) {
            //secured
            cell.spawnTeamParticles(cell.getOwner(), gameSpace.getWorld());
        }
    }

    private void tickCapturing(Cell cell, HashSet<ServerPlayerEntity> attackers) {
        if (cell.captureTicks == 0) {
            //began capturing
        }

        GameTeam captureTeam = game.getParticipant(attackers.iterator().next()).team;
        ServerWorld world = gameSpace.getWorld();

        if (cell.incrementCapture(captureTeam, world, attackers.size(), game.getMap().cellManager)) {
            //captured
            cell.spawnTeamParticles(captureTeam, world);
            cell.setModuleColor(captureTeam == FortressTeams.RED ? FortressTeams.RED_PALLET : FortressTeams.BLUE_PALLET, world);

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

                    Text rowCaptured = new LiteralText("⛏ ")
                            .setStyle(Fortress.PREFIX_STYLE)
                            .append(new TranslatableText("text.fortress.row_captured").formatted(captureTeam.getFormatting()));

                    Text randomModule = new LiteralText("⚅ ")
                            .setStyle(Fortress.PREFIX_STYLE)
                            .append(new TranslatableText("text.fortress.give_module", firstAttacker.getDisplayName(), stack.toHoverableText())
                                    .formatted(captureTeam.getFormatting()));

                    gameSpace.getPlayers().sendMessage(rowCaptured);
                    gameSpace.getPlayers().sendMessage(randomModule);

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
