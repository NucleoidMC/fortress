package us.potatoboy.fortress.game.active;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.ServerWorldAccess;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.event.UseItemOnBlockListener;
import us.potatoboy.fortress.game.*;
import us.potatoboy.fortress.game.map.FortressMap;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Map;

public class FortressActive {
    private final FortressConfig config;

    public final GameSpace gameSpace;
    private final FortressMap map;

    private final FortressTeams teams;

    public final Object2ObjectMap<PlayerRef, FortressPlayer> participants;

    final ModuleManager moduleManager;
    final CaptureManager captureManager;
    final FortressStateManager stateManager;

    final FortressSidebar sidebar;

    private final FortressKit fortressKit;

    private FortressActive(GameSpace gameSpace, FortressMap map, FortressConfig config, GlobalWidgets widgets, Multimap<GameTeam, ServerPlayerEntity> players, ModuleManager moduleManager) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.map = map;
        this.moduleManager = moduleManager;
        this.participants = new Object2ObjectOpenHashMap<>();
        this.captureManager = new CaptureManager(this);
        this.stateManager = new FortressStateManager(this);

        this.teams = gameSpace.addResource(new FortressTeams(gameSpace));

        for (GameTeam team : players.keySet()) {
            for (ServerPlayerEntity playerEntity : players.get(team)) {
                this.participants.put(PlayerRef.of(playerEntity), new FortressPlayer(team));
                this.teams.addPlayer(playerEntity, team);
            }
        }
        
        captureManager.setRowCaptured(FortressTeams.BLUE, 0);
        captureManager.setRowCaptured(FortressTeams.RED, map.cellManager.cells.length - 1);

        this.sidebar = new FortressSidebar(this, widgets);
        this.fortressKit = new FortressKit(gameSpace.getWorld());
    }

    public static void open(GameSpace gameSpace, FortressMap map, FortressConfig config, Multimap<GameTeam, ServerPlayerEntity> players, ModuleManager moduleManager) {
        gameSpace.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);

            FortressActive active = new FortressActive(gameSpace, map, config, widgets, players, moduleManager);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
            game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
            game.setRule(GameRule.PLACE_BLOCKS, RuleResult.ALLOW);
            game.setRule(GameRule.BREAK_BLOCKS, RuleResult.ALLOW);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);
            game.on(PlaceBlockListener.EVENT, active::onPlaceBlock);
            game.on(UseItemOnBlockListener.EVENT, active::onUseItemOnBlock);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);
            
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private ActionResult onUseItemOnBlock(ServerPlayerEntity player, BlockPos pos, ItemUsageContext context) {
        if (context.getStack().getItem() instanceof ModuleItem) {

            ItemStack itemStack = context.getStack();

            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (context.getWorld().canPlayerModifyAt(player, context.getBlockPos()) && player.canPlaceOn(blockPos2, direction, itemStack)) {
                Cell cell = map.cellManager.getCell(blockPos);
                Structure structure = moduleManager.getStructure((ModuleItem) context.getStack().getItem());
                if (cell == null || cell.isOccupied() || structure == null || cell.getOwner() != getParticipant(player).team || cell.captureState != null) {
                    int slot;
                    if (context.getHand() == Hand.MAIN_HAND) {
                        slot = player.inventory.selectedSlot;
                    } else {
                        slot = 40; // offhand
                    }

                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, slot, context.getStack()));
                    return ActionResult.FAIL;
                }

                StructurePlacementData structurePlacementData = new StructurePlacementData();
                BlockPos structurePos = new BlockPos(cell.getCenter()).add(0, 1, 0);
                Direction playerDirection = context.getPlayerFacing();
                switch (playerDirection) {
                    case NORTH:
                        structurePos = structurePos.add(-1, 0, -1);
                        break;
                    case SOUTH:
                        structurePlacementData.setMirror(BlockMirror.LEFT_RIGHT);
                        structurePos = structurePos.add(-1, 0, 1);
                        break;
                    case WEST:
                        structurePlacementData.setRotation(BlockRotation.COUNTERCLOCKWISE_90);
                        structurePos = structurePos.add(-1, 0, 1);
                        break;
                    case EAST:
                        structurePlacementData.setRotation(BlockRotation.CLOCKWISE_90);
                        structurePos = structurePos.add(1, 0, -1);
                        break;
                }

                structure.place((ServerWorldAccess) context.getWorld(), structurePos, structurePlacementData, player.getRandom());
                context.getStack().decrement(1);
                cell.setOccupied(true);
                return ActionResult.SUCCESS;
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private void tick() {
        ServerWorld world = gameSpace.getWorld();
        long time = world.getTime();

        FortressStateManager.TickResult result = stateManager.tick(time);
        if (result != FortressStateManager.TickResult.CONTINUE_TICK) {
            switch (result) {
                case RED_WIN:
                    broadcastWin(FortressTeams.RED);
                    break;
                case BLUE_WIN:
                    broadcastWin(FortressTeams.BLUE);
                    break;
                case GAME_CLOSED:
                    gameSpace.close(GameCloseReason.FINISHED);
                    break;
            }

            return;
        }

        if (time % 20 == 0) {
            captureManager.tick(world, 20);
            sidebar.update(time);
        }

        tickDead(world, time);
    }

    private void tickDead(ServerWorld world, long time) {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            PlayerRef ref = entry.getKey();
            FortressPlayer state = entry.getValue();

            ref.ifOnline(world, player -> {
                if (player.isSpectator()) {
                    int respawnDelay = 5;

                    int sec = respawnDelay - (int) Math.floor((time - state.timeOfDeath) / 20.0F);

                    if (sec > 0 && (time - state.timeOfDeath) % 20 == 0) {
                        Text text = new LiteralText(String.format("Respawning in %ds", sec)).formatted(Formatting.BOLD);
                        player.sendMessage(text, true);
                    }

                    if (time - state.timeOfDeath > respawnDelay * 20) {
                        this.spawnParticipant(player);
                    }
                }
            });
        }
    }

    private void broadcastWin(GameTeam winTeam) {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(this.gameSpace.getServer(), player -> {
                if (entry.getValue().team == winTeam) {
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0F, 1.0F);
                } else {
                    player.playSound(SoundEvents.ENTITY_DONKEY_DEATH, SoundCategory.MASTER, 1.0F, 1.0F);
                }
            });
        }

        Text title = new LiteralText("")
                .append(winTeam.getDisplay())
                .append(" Wins!")
                .formatted(Formatting.BOLD, winTeam.getFormatting());

        PlayerSet players = gameSpace.getPlayers();
        players.sendTitle(title, 1, 200, 3);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource source) {
        MutableText deathMessage = getDeathMessage(playerEntity, source);
        gameSpace.getPlayers().sendMessage(deathMessage.formatted(Formatting.GRAY));
        if (source.getAttacker() != null && source.getAttacker() instanceof ServerPlayerEntity) {
            ServerPlayerEntity attacker = (ServerPlayerEntity) source.getAttacker();
            FortressPlayer participant = getParticipant(attacker);

            participant.giveModule(attacker, participant.team, FortressModules.getRandomModule(attacker.getRandom()), 1);
            participant.kills += 1;
        }

        spawnDeadParticipant(playerEntity);
        return ActionResult.FAIL;
    }

    private MutableText getDeathMessage(ServerPlayerEntity player, DamageSource source) {
        FortressPlayer participant = getParticipant(player);
        ServerWorld world = gameSpace.getWorld();
        long time = world.getTime();

        MutableText elimMessage = new LiteralText(" was killed by ");
        if (source.getAttacker() != null) {
            elimMessage.append(source.getAttacker().getDisplayName());
        } else if (source == DamageSource.IN_WALL) {
            elimMessage = new LiteralText(" didn't stand back");
        } else {
            elimMessage = new LiteralText(" died");
        }

        return new LiteralText("").append(player.getDisplayName()).append(elimMessage);
    }

    private void removePlayer(ServerPlayerEntity playerEntity) {
    }

    private void addPlayer(ServerPlayerEntity playerEntity) {
        if (participants.containsKey(PlayerRef.of(playerEntity))) {
            playerEntity.inventory.clear();

            spawnParticipant(playerEntity);
            sidebar.sidebars.get(getParticipant(playerEntity)).addPlayer(playerEntity);
            fortressKit.giveItems(playerEntity, getParticipant(playerEntity).team);
        } else {
            FortressSpawnLogic.resetPlayer(playerEntity, GameMode.SPECTATOR);
        }
    }

    private ActionResult onPlaceBlock(ServerPlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, ItemUsageContext itemUsageContext) {
        return ActionResult.PASS;
    }

    private void onClose() {
        sidebar.close();
    }

    private void onOpen() {
        ServerWorld world = gameSpace.getWorld();
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(world, this::spawnParticipant);
        }

        fortressKit.giveStarterKit(participants);

        stateManager.onOpen(world.getTime(), config);
    }

    public FortressPlayer getParticipant(ServerPlayerEntity player) {
        return getParticipant(PlayerRef.of(player));
    }

    public FortressPlayer getParticipant(PlayerRef player) {
        return participants.get(player);
    }

    private void spawnDeadParticipant(ServerPlayerEntity player) {
        player.setGameMode(GameMode.SPECTATOR);

        FortressPlayer fortressPlayer = getParticipant(player);
        if (fortressPlayer != null) {
            fortressPlayer.timeOfDeath = gameSpace.getWorld().getTime();
        }
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        FortressPlayer participant = getParticipant(player);
        assert participant != null;
        participant.timeOfSpawn = gameSpace.getWorld().getTime();

        FortressSpawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.getSpawn(participant.team, player.getRandom()), gameSpace.getWorld(), participant.team == FortressTeams.RED ? 180.0f : 0.0f);
    }

    public FortressMap getMap() {
        return map;
    }
}
