package us.potatoboy.fortress.game.active;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import us.potatoboy.fortress.Fortress;
import us.potatoboy.fortress.FortressStatistics;
import us.potatoboy.fortress.custom.item.FortressModules;
import us.potatoboy.fortress.custom.item.ModuleItem;
import us.potatoboy.fortress.game.Cell;
import us.potatoboy.fortress.game.FortressConfig;
import us.potatoboy.fortress.game.FortressSpawnLogic;
import us.potatoboy.fortress.game.FortressTeams;
import us.potatoboy.fortress.game.map.FortressMap;
import us.potatoboy.fortress.utility.TextUtil;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.stats.StatisticKeys;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockPunchEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ArrowFireEvent;

import java.util.Map;

public class FortressActive {
    public final FortressConfig config;

    public final GameSpace gameSpace;
    public final ServerWorld world;
    public final FortressTeams teams;
    private final FortressMap map;

    public final Object2ObjectMap<PlayerRef, FortressPlayer> participants;

    final CaptureManager captureManager;
    final FortressStateManager stateManager;
    public final GameStatisticBundle statistics;

    protected final Sidebar globalSidebar = new Sidebar(Sidebar.Priority.MEDIUM);

    private final FortressKit fortressKit;

    private FortressActive(GameSpace gameSpace, ServerWorld world, FortressMap map, FortressConfig config, GlobalWidgets widgets, Multimap<GameTeamKey, ServerPlayerEntity> players, FortressTeams teams) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.config = config;
        this.map = map;
        this.teams = teams;
        this.participants = new Object2ObjectOpenHashMap<>();
        this.captureManager = new CaptureManager(this);
        this.stateManager = new FortressStateManager(this);
        this.statistics = gameSpace.getStatistics().bundle(Fortress.ID);

        for (GameTeamKey team : players.keySet()) {
            for (ServerPlayerEntity playerEntity : players.get(team)) {
                this.participants.put(PlayerRef.of(playerEntity), new FortressPlayer(team));
                this.teams.addPlayer(playerEntity, team);
                this.statistics.forPlayer(playerEntity).increment(StatisticKeys.GAMES_PLAYED, 1);
            }
        }

        captureManager.setRowCaptured(FortressTeams.BLUE.key(), 0);
        captureManager.setRowCaptured(FortressTeams.RED.key(), map.cellManager.cells.length - 1);

        buildSidebar();
        globalSidebar.show();

        this.fortressKit = new FortressKit(world, teams);
    }

    private void buildSidebar() {
        this.globalSidebar.setTitle(TextUtil.getText("sidebar", "title").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));

        this.globalSidebar.set(builder -> {
            builder.add(player -> {
                long ticksUntilEnd = Math.max(stateManager.finishTime - world.getTime(), 0);
                long secondsUntilEnd = ticksUntilEnd / 20;

                long minutes = secondsUntilEnd / 60;
                long seconds = secondsUntilEnd % 60;

                return TextUtil.getText("sidebar", "time_left", Text.literal(String.format("%02d:%02d", minutes, seconds)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xd9d9d9)));
            });

            builder.add(Text.empty());


            builder.add(player -> {
                Pair<Integer, Integer> percents = map.getControlPercent();
                return TextUtil.getText("sidebar", "percent.red", Text.literal(percents.getLeft().toString() + "%").formatted(Formatting.GREEN)).formatted(Formatting.RED);
            });
            builder.add(player -> {
                Pair<Integer, Integer> percents = map.getControlPercent();
                return TextUtil.getText("sidebar", "percent.blue", Text.literal(percents.getRight().toString() + "%").formatted(Formatting.GREEN)).formatted(Formatting.BLUE);
            });

            builder.add(Text.empty());

            builder.add(player -> {
                FortressPlayer participant = participants.get(PlayerRef.of(player));

                return TextUtil.getText("sidebar", "stats",
                        Text.literal("" + participant.kills).formatted(Formatting.GREEN),
                        Text.literal("" + participant.deaths).formatted(Formatting.GREEN),
                        Text.literal("" + participant.captures).formatted(Formatting.GREEN)
                ).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xd9d9d9)));
            });
        });
    }

    public static void open(GameSpace gameSpace, ServerWorld world, FortressMap map, FortressConfig config, Multimap<GameTeamKey, ServerPlayerEntity> players) {
        gameSpace.setActivity(game -> {
            var widgets = GlobalWidgets.addTo(game);

            var teams = new FortressTeams(gameSpace);
            teams.applyTo(game);

            FortressActive active = new FortressActive(gameSpace, world, map, config, widgets, players, teams);

            game.deny(GameRuleType.CRAFTING);
            game.deny(GameRuleType.PORTALS);
            game.allow(GameRuleType.PVP);
            game.deny(GameRuleType.HUNGER);
            game.allow(GameRuleType.INTERACTION);
            game.allow(GameRuleType.FALL_DAMAGE);
            game.allow(GameRuleType.PLACE_BLOCKS);
            game.allow(GameRuleType.BREAK_BLOCKS);
            game.deny(GameRuleType.THROW_ITEMS);

            game.listen(GameActivityEvents.ENABLE, active::onOpen);
            game.listen(GameActivityEvents.DISABLE, active::onClose);
            game.listen(BlockPlaceEvent.BEFORE, active::onPlaceBlock);
            game.listen(BlockUseEvent.EVENT, active::onUseBlock);
            game.listen(ArrowFireEvent.EVENT, active::onFireArrow);
            game.listen(BlockPunchEvent.EVENT, active::onAttackBlock);

            game.listen(GameActivityEvents.TICK, active::tick);

            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, FortressSpawnLogic.choosePos(offer.player().getRandom(), map.waitingSpawn, 0f)));
            game.listen(GamePlayerEvents.ADD, active::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            game.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
        });
    }

    private ActionResult onAttackBlock(ServerPlayerEntity playerEntity, Direction direction, BlockPos blockPos) {
        return ActionResult.FAIL;
    }

    private ActionResult onFireArrow(ServerPlayerEntity player, ItemStack itemStack, ArrowItem arrowItem, int i, PersistentProjectileEntity persistentProjectileEntity) {
        ItemCooldownManager cooldown = player.getItemCooldownManager();
        if (!cooldown.isCoolingDown(itemStack.getItem())) {
            cooldown.set(itemStack.getItem(), 60);
        }
        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);

        if (map.cellManager.getCell(hitResult.getBlockPos()) == null) return ActionResult.FAIL;

        if (stack.getItem() instanceof ModuleItem moduleItem) {
            BlockPos blockPos = hitResult.getBlockPos();
            Direction direction = hitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (world.canPlayerModifyAt(player, hitResult.getBlockPos()) && player.canPlaceOn(blockPos2, direction, stack)) {
                Cell cell = map.cellManager.getCell(blockPos);
                StructureTemplate structure = moduleItem.getStructure(gameSpace.getServer());

                int placeIndex = (blockPos.getY() - map.cellManager.getFloorHeight()) / 3;

                if (cell == null
                        || !cell.enabled
                        || cell.hasModuleAt(placeIndex)
                        || structure == null
                        || cell.getOwner() != getParticipant(player).team
                        || cell.captureState != null
                        || (blockPos.getY() - map.cellManager.getFloorHeight() + 3) > config.mapConfig().buildLimit()
                ) {
                    int slot;
                    if (hand == Hand.MAIN_HAND) {
                        slot = player.getInventory().selectedSlot;
                    } else {
                        slot = 40; // offhand
                    }

                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, stack));
                    return ActionResult.FAIL;
                }

                StructurePlacementData structurePlacementData = new StructurePlacementData();
                BlockPos structurePos = new BlockPos(cell.getCenter()).add(0, 1, 0).add(0, placeIndex * 3, 0);
                BlockPos structurePivot = new BlockPos(structurePos);
                Direction playerDirection = player.getHorizontalFacing();
                switch (playerDirection) {
                    case NORTH -> structurePos = structurePos.add(-1, 0, -1);
                    case SOUTH -> {
                        structurePlacementData.setMirror(BlockMirror.LEFT_RIGHT);
                        structurePos = structurePos.add(-1, 0, 1);
                    }
                    case WEST -> {
                        structurePlacementData.setRotation(BlockRotation.COUNTERCLOCKWISE_90);
                        structurePos = structurePos.add(-1, 0, 1);
                    }
                    case EAST -> {
                        structurePlacementData.setRotation(BlockRotation.CLOCKWISE_90);
                        structurePos = structurePos.add(1, 0, -1);
                    }
                }

                structure.place(world, structurePos, structurePivot, structurePlacementData, player.getRandom(), Block.NOTIFY_LISTENERS);

                ParticleEffect effect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState());
                cell.spawnParticles(effect, world);

                stack.decrement(1);
                cell.addModule(moduleItem);
                cell.setModuleColor(cell.getOwner() == FortressTeams.RED.key() ? FortressTeams.RED_PALLET : FortressTeams.BLUE_PALLET, world);

                statistics.forPlayer(player).increment(FortressStatistics.MODULES_PLACED, 1);
                return ActionResult.SUCCESS;
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private void tick() {
        long time = world.getTime();

        if (time % config.captureTickDelay() == 0) {
            captureManager.tick(world);
        }

        if (time % 20 == 0) {

            Cell[][] cells = map.cellManager.cells;
            for (Cell[] row : cells) {
                for (Cell cell : row) {
                    cell.tickModules(participants, world);
                }
            }
        }

        FortressStateManager.TickResult result = stateManager.tick(time);
        if (result != FortressStateManager.TickResult.CONTINUE_TICK) {
            switch (result) {
                case RED_WIN -> broadcastWin(FortressTeams.RED);
                case BLUE_WIN -> broadcastWin(FortressTeams.BLUE);
                case GAME_CLOSED -> gameSpace.close(GameCloseReason.FINISHED);
            }

            return;
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
                        Text text = Text.translatable("text.fortress.respawning", sec).formatted(Formatting.BOLD);
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
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            if (participants.containsKey(PlayerRef.of(player))) {
                var participant = getParticipant(player);
                if (participant.team == winTeam.key()) {
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0F, 1.0F);
                    this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_WON, 1);
                } else {
                    player.playSound(SoundEvents.ENTITY_DONKEY_DEATH, SoundCategory.MASTER, 1.0F, 1.0F);
                    this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_LOST, 1);
                }
            }
        }

        PlayerRef mostKills = null;
        PlayerRef mostCaptures = null;

        for (PlayerRef player : participants.keySet()) {
            if (mostKills == null) {
                mostKills = player;
                mostCaptures = player;
            }

            if (participants.get(player).kills > participants.get(mostKills).kills) {
                mostKills = player;
            }

            if (participants.get(player).captures > participants.get(mostCaptures).captures) {
                mostCaptures = player;
            }
        }

        Text title = Text.translatable("text.fortress.wins", winTeam.config().name())
                .formatted(Formatting.BOLD, winTeam.config().chatFormatting());

        Text kills = Text.translatable("text.fortress.most_kills",
                participants.get(mostKills).displayName,
                participants.get(mostKills).kills);

        Text captures = Text.translatable("text.fortress.most_captures",
                participants.get(mostCaptures).displayName,
                participants.get(mostCaptures).captures);

        PlayerSet players = gameSpace.getPlayers();
        players.showTitle(title, 1, 200, 3);
        players.sendMessage(Text.literal("------------------"));
        players.sendMessage(title);
        players.sendMessage(kills);
        players.sendMessage(captures);
        players.sendMessage(Text.literal("------------------"));
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource source) {
        Text deathMessage = getDeathMessage(playerEntity, source);
        gameSpace.getPlayers().sendMessage(deathMessage);
        getParticipant(playerEntity).deaths += 1;
        this.statistics.forPlayer(playerEntity).increment(StatisticKeys.DEATHS, 1);

        for (int i = 0; i < 75; i++) {
            world.spawnParticles(
                    ParticleTypes.FIREWORK,
                    playerEntity.getPos().getX(),
                    playerEntity.getPos().getY() + 1.0f,
                    playerEntity.getPos().getZ(),
                    1,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    0.1);
        }

        if (source.getAttacker() != null && source.getAttacker() instanceof ServerPlayerEntity attacker) {
            FortressPlayer participant = getParticipant(attacker);

            if (participant != null) {
                participant.giveModule(attacker, participant.team, FortressModules.getRandomModule(attacker.getRandom()), 1);
                participant.kills += 1;
                this.statistics.forPlayer(attacker).increment(StatisticKeys.KILLS, 1);
            }
        }

        spawnDeadParticipant(playerEntity);
        return ActionResult.FAIL;
    }

    private Text getDeathMessage(ServerPlayerEntity player, DamageSource source) {
        Text deathMes = source.getDeathMessage(player);

        return Text.literal("☠ ").setStyle(Fortress.PREFIX_STYLE).append(deathMes.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf))));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        this.statistics.forPlayer(player).increment(StatisticKeys.DAMAGE_TAKEN, amount);

        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            this.statistics.forPlayer(attacker).increment(StatisticKeys.DAMAGE_DEALT, amount);
        }

        return ActionResult.PASS;
    }

    private void removePlayer(ServerPlayerEntity playerEntity) {
        globalSidebar.removePlayer(playerEntity);
    }

    private void addPlayer(ServerPlayerEntity playerEntity) {
        if (participants.containsKey(PlayerRef.of(playerEntity))) {
            playerEntity.getInventory().clear();

            spawnParticipant(playerEntity);
            globalSidebar.addPlayer(playerEntity);
            fortressKit.giveItems(playerEntity, getParticipant(playerEntity).team);
        } else {
            if (config.midJoin()) {
                GameTeamKey team = teams.getSmallestTeam(playerEntity.getRandom());
                this.participants.put(PlayerRef.of(playerEntity), new FortressPlayer(team));
                this.teams.addPlayer(playerEntity, team);
                globalSidebar.addPlayer(playerEntity);
                this.statistics.forPlayer(playerEntity).increment(StatisticKeys.GAMES_PLAYED, 1);

                playerEntity.getInventory().clear();
                spawnParticipant(playerEntity);
                fortressKit.giveItems(playerEntity, getParticipant(playerEntity).team);
            } else {
                FortressSpawnLogic.resetPlayer(playerEntity, GameMode.SPECTATOR);
            }
        }
    }

    private ActionResult onPlaceBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext context) {
        return ActionResult.PASS;
    }

    private void onClose() {
        globalSidebar.hide();
    }

    private void onOpen() {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(world, this::spawnParticipant);
            entry.getValue().displayName = entry.getKey().getEntity(world).getDisplayName();
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
        player.changeGameMode(GameMode.SPECTATOR);

        FortressPlayer fortressPlayer = getParticipant(player);
        if (fortressPlayer != null) {
            fortressPlayer.timeOfDeath = world.getTime();
        }
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        FortressPlayer participant = getParticipant(player);
        assert participant != null;
        participant.timeOfSpawn = world.getTime();

        FortressSpawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.getSpawn(participant.team, player.getRandom()), world, participant.team == FortressTeams.RED.key() ? 180.0f : 0.0f);
    }

    public FortressMap getMap() {
        return map;
    }
}
