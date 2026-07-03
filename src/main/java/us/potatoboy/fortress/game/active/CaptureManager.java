package us.potatoboy.fortress.game.active;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import us.potatoboy.fortress.utility.Tuple;
import net.minecraft.world.level.GameType;
import us.potatoboy.fortress.Fortress;
import us.potatoboy.fortress.FortressStatistics;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.CaptureState;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.CellManager;
import us.potatoboy.fortress.game.FortressTeams;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.HashMap;
import java.util.HashSet;

public class CaptureManager {
    private final GameSpace gameSpace;
    private final FortressActive game;

    private HashMap<GameTeamKey, HashSet<Integer>> capturedRows = new HashMap<>();

    CaptureManager(FortressActive game) {
        this.gameSpace = game.gameSpace;
        this.game = game;
    }

    public void tick(ServerLevel level) {
        HashMap<Cell, HashSet<ServerPlayer>> cells = new HashMap<>();

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(game.participants)) {
            ServerPlayer player = entry.getKey().getEntity(level);
            if (player == null) continue;
            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) continue;

            FortressPlayer participant = entry.getValue();

            Cell currentCell = game.getMap().cellManager.getCell(player.blockPosition());

            if (currentCell == null) continue;
            if (!currentCell.enabled) continue;

            if (!game.config.recapture() && currentCell.getOwner() != null) continue;

            boolean ownsNeighbor = false;

            Tuple<Integer, Integer> location = game.getMap().cellManager.getCellPos(player.blockPosition());
            Cell[][] mapCells = game.getMap().cellManager.cells;
            if (location == null) continue;

            for (int x = location.getA() - 1; x <= (location.getA()) + 1; x += 1) {
                for (int z = location.getB() - 1; z <= (location.getB() + 1); z += 1) {
                    if (x < 0 || z < 0 || x >= mapCells.length || z >= mapCells[x].length) {
                        continue;
                    }

                    Cell neighborCell = game.getMap().cellManager.cells[x][z];
                    if (neighborCell.getOwner() == participant.team) {
                        ownsNeighbor = true;
                    }
                }
            }

            if (game.config.captureEnemy()) {
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

    private void tickCell(Cell cell, HashSet<ServerPlayer> players) {
        HashSet<ServerPlayer> defenders = new HashSet<>();
        HashSet<ServerPlayer> attackers = new HashSet<>();

        for (ServerPlayer player : players) {
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
        ServerLevel level = game.level;

        cell.spawnParticles(ParticleTypes.ANGRY_VILLAGER, level);
    }

    private void tickSecuring(Cell cell, HashSet<ServerPlayer> defenders) {
        if (cell.decrementCapture(game.level, defenders.size(), game.getMap().cellManager)) {
            //secured
            cell.spawnTeamParticles(game.teams.getConfig(cell.getOwner()), game.level);
        }
    }

    private void tickCapturing(Cell cell, HashSet<ServerPlayer> attackers) {
        if (cell.captureTicks == 0) {
            //began capturing
        }

        GameTeamKey captureTeam = game.getParticipant(attackers.iterator().next()).team;
        GameTeamConfig teamConfig = game.teams.getConfig(captureTeam);
        ServerLevel level = game.level;

        if (cell.incrementCapture(captureTeam, level, attackers.size(), game.getMap().cellManager)) {
            //captured
            cell.spawnTeamParticles(teamConfig, level);
            cell.setModuleColor(captureTeam == FortressTeams.RED.key() ? FortressTeams.RED_PALLET : FortressTeams.BLUE_PALLET, level);

            CellManager cellManager = game.getMap().cellManager;
            int cellCollum = cellManager.getCellPos(cell.getCenter()).getA();
            if (cellManager.checkRow(cellCollum, captureTeam)) {
                capturedRows.putIfAbsent(captureTeam, new HashSet<>());

                if (!capturedRows.get(captureTeam).contains(cellCollum)) {
                    //Captured row for the first time
                    capturedRows.get(captureTeam).add(cellCollum);

                    for (Cell rowCell : cellManager.cells[cellCollum]) {
                        rowCell.spawnTeamParticles(teamConfig, level);
                    }

                    ServerPlayer firstAttacker = attackers.iterator().next();
                    ModuleItem moduleItem = FortressModules.getRandomSpecial(firstAttacker.getRandom());
                    ItemStack stack = new ItemStack(moduleItem);

                    Component rowCaptured = Component.literal("⛏ ")
                            .setStyle(Fortress.PREFIX_STYLE)
                            .append(Component.translatable("text.fortress.row_captured").withStyle(teamConfig.chatFormatting()));

                    Component randomModule = Component.literal("⚅ ")
                            .setStyle(Fortress.PREFIX_STYLE)
                            .append(Component.translatable("text.fortress.give_module", firstAttacker.getDisplayName(), stack.getDisplayName())
                                    .withStyle(teamConfig.chatFormatting()));

                    gameSpace.getPlayers().sendMessage(rowCaptured);
                    gameSpace.getPlayers().sendMessage(randomModule);

                    game.getParticipant(firstAttacker).giveModule(firstAttacker, captureTeam, moduleItem, 1);
                    game.statistics.forPlayer(firstAttacker).increment(FortressStatistics.ROWS_CAPTURED, 1);
                }
            }

            for (ServerPlayer attacker : attackers) {
                game.getParticipant(attacker).captures++;
                game.statistics.forPlayer(attacker).increment(FortressStatistics.CAPTURES, 1);
            }
        } else {
            //capturing
        }
    }

    public void setRowCaptured(GameTeamKey team, int collum) {
        capturedRows.putIfAbsent(team, new HashSet<>());
        capturedRows.get(team).add(collum);
    }
}
