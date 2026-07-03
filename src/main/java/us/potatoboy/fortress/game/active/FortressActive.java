package us.potatoboy.fortress.game.active;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import us.potatoboy.fortress.utility.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
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
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKeys;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
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
    public final ServerLevel level;
    public final FortressTeams teams;
    private final FortressMap map;

    public final Object2ObjectMap<PlayerRef, FortressPlayer> participants;

    final CaptureManager captureManager;
    final FortressStateManager stateManager;
    public final GameStatisticBundle statistics;

    protected final Sidebar globalSidebar = new Sidebar(Sidebar.Priority.MEDIUM);

    private final FortressKit fortressKit;

    private FortressActive(GameSpace gameSpace, ServerLevel level, FortressMap map, FortressConfig config, GlobalWidgets widgets, Multimap<GameTeamKey, ServerPlayer> players, FortressTeams teams) {
        this.gameSpace = gameSpace;
        this.level = level;
        this.config = config;
        this.map = map;
        this.teams = teams;
        this.participants = new Object2ObjectOpenHashMap<>();
        this.captureManager = new CaptureManager(this);
        this.stateManager = new FortressStateManager(this);
        this.statistics = gameSpace.getStatistics().bundle(Fortress.ID);

        for (GameTeamKey team : players.keySet()) {
            for (ServerPlayer playerEntity : players.get(team)) {
                this.participants.put(PlayerRef.of(playerEntity), new FortressPlayer(team));
                this.teams.addPlayer(playerEntity, team);
                this.statistics.forPlayer(playerEntity).increment(StatisticKeys.GAMES_PLAYED, 1);
            }
        }

        captureManager.setRowCaptured(FortressTeams.BLUE.key(), 0);
        captureManager.setRowCaptured(FortressTeams.RED.key(), map.cellManager.cells.length - 1);

        buildSidebar();
        globalSidebar.show();

        this.fortressKit = new FortressKit(level, teams);
    }

    private void buildSidebar() {
        this.globalSidebar.setTitle(TextUtil.getText("sidebar", "title").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));

        this.globalSidebar.set(builder -> {
            builder.add(player -> {
                long ticksUntilEnd = Math.max(stateManager.finishTime - level.getGameTime(), 0);
                long secondsUntilEnd = ticksUntilEnd / 20;

                long minutes = secondsUntilEnd / 60;
                long seconds = secondsUntilEnd % 60;

                return TextUtil.getText("sidebar", "time_left", Component.literal(String.format("%02d:%02d", minutes, seconds)).withStyle(ChatFormatting.GREEN)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xd9d9d9)));
            });

            builder.add(Component.empty());


            builder.add(player -> {
                Tuple<Integer, Integer> percents = map.getControlPercent();
                return TextUtil.getText("sidebar", "percent.red", Component.literal(percents.getA().toString() + "%").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.RED);
            });
            builder.add(player -> {
                Tuple<Integer, Integer> percents = map.getControlPercent();
                return TextUtil.getText("sidebar", "percent.blue", Component.literal(percents.getB().toString() + "%").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.BLUE);
            });

            builder.add(Component.empty());

            builder.add(player -> {
                FortressPlayer participant = participants.get(PlayerRef.of(player));

                return TextUtil.getText("sidebar", "stats",
                        Component.literal("" + participant.kills).withStyle(ChatFormatting.GREEN),
                        Component.literal("" + participant.deaths).withStyle(ChatFormatting.GREEN),
                        Component.literal("" + participant.captures).withStyle(ChatFormatting.GREEN)
                ).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xd9d9d9)));
            });
        });
    }

    public static void open(GameSpace gameSpace, ServerLevel level, FortressMap map, FortressConfig config, Multimap<GameTeamKey, ServerPlayer> players) {
        gameSpace.setActivity(game -> {
            var widgets = GlobalWidgets.addTo(game);

            var teams = new FortressTeams(gameSpace);
            teams.applyTo(game);

            FortressActive active = new FortressActive(gameSpace, level, map, config, widgets, players, teams);

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

            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(level, FortressSpawnLogic.choosePos(map.waitingSpawn, 0f)));
            game.listen(GamePlayerEvents.ADD, active::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            game.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
        });
    }

    private EventResult onAttackBlock(ServerPlayer playerEntity, Direction direction, BlockPos blockPos) {
        return EventResult.DENY;
    }

    private EventResult onFireArrow(ServerPlayer player, ItemStack itemStack, ArrowItem arrowItem, int i, AbstractArrow persistentProjectileEntity) {
        ItemCooldowns cooldown = player.getCooldowns();
        if (!cooldown.isOnCooldown(itemStack)) {
            cooldown.addCooldown(itemStack, 60);
        }
        return EventResult.PASS;
    }

    private InteractionResult onUseBlock(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);

        if (map.cellManager.getCell(hitResult.getBlockPos()) == null) return InteractionResult.FAIL;

        if (stack.getItem() instanceof ModuleItem moduleItem) {
            BlockPos blockPos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.mayInteract(player, hitResult.getBlockPos()) && player.mayUseItemAt(blockPos2, direction, stack)) {
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
                    if (hand == InteractionHand.MAIN_HAND) {
                        slot = player.getInventory().getSelectedSlot();
                    } else {
                        slot = 40; // offhand
                    }

                    player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slot, stack));
                    return InteractionResult.FAIL;
                }

                StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
                BlockPos structurePos = new BlockPos(cell.getCenter()).offset(0, 1, 0).offset(0, placeIndex * 3, 0);
                BlockPos structurePivot = new BlockPos(structurePos);
                Direction playerDirection = player.getDirection();
                switch (playerDirection) {
                    case NORTH -> structurePos = structurePos.offset(-1, 0, -1);
                    case SOUTH -> {
                        structurePlacementData.setMirror(Mirror.LEFT_RIGHT);
                        structurePos = structurePos.offset(-1, 0, 1);
                    }
                    case WEST -> {
                        structurePlacementData.setRotation(Rotation.COUNTERCLOCKWISE_90);
                        structurePos = structurePos.offset(-1, 0, 1);
                    }
                    case EAST -> {
                        structurePlacementData.setRotation(Rotation.CLOCKWISE_90);
                        structurePos = structurePos.offset(1, 0, -1);
                    }
                }

                structure.placeInWorld(level, structurePos, structurePivot, structurePlacementData, player.getRandom(), Block.UPDATE_CLIENTS);

                ParticleOptions effect = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState());
                cell.spawnParticles(effect, level);

                stack.shrink(1);
                cell.addModule(moduleItem);
                cell.setModuleColor(cell.getOwner() == FortressTeams.RED.key() ? FortressTeams.RED_PALLET : FortressTeams.BLUE_PALLET, level);

                statistics.forPlayer(player).increment(FortressStatistics.MODULES_PLACED, 1);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    private void tick() {
        long time = level.getGameTime();

        if (time % config.captureTickDelay() == 0) {
            captureManager.tick(level);
        }

        if (time % 20 == 0) {

            Cell[][] cells = map.cellManager.cells;
            for (Cell[] row : cells) {
                for (Cell cell : row) {
                    cell.tickModules(participants, level);
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

        tickDead(level, time);
    }

    private void tickDead(ServerLevel level, long time) {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            PlayerRef ref = entry.getKey();
            FortressPlayer state = entry.getValue();

            ref.ifOnline(level, player -> {
                if (player.isSpectator()) {
                    int respawnDelay = 5;

                    int sec = respawnDelay - (int) Math.floor((time - state.timeOfDeath) / 20.0F);

                    if (sec > 0 && (time - state.timeOfDeath) % 20 == 0) {
                        Component text = Component.translatable("text.fortress.respawning", sec).withStyle(ChatFormatting.BOLD);
                        player.sendSystemMessage(text, true);
                    }

                    if (time - state.timeOfDeath > respawnDelay * 20) {
                        this.spawnParticipant(player);
                    }
                }
            });
        }
    }

    private void broadcastWin(GameTeam winTeam) {
        for (ServerPlayer player : gameSpace.getPlayers()) {
            if (participants.containsKey(PlayerRef.of(player))) {
                var participant = getParticipant(player);
                if (participant.team == winTeam.key()) {
                    Vec3 pos = player.position();
                    player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), SoundSource.MASTER, pos.x(), pos.y(), pos.z(), 1.0F, 1.0F, level.getRandom().nextLong()));
                    this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_WON, 1);
                } else {
                    Vec3 pos = player.position();
                    player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.DONKEY_DEATH), SoundSource.MASTER, pos.x(), pos.y(), pos.z(), 1.0F, 1.0F, level.getRandom().nextLong()));
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

        Component title = Component.translatable("text.fortress.wins", winTeam.config().name())
                .withStyle(ChatFormatting.BOLD, winTeam.config().chatFormatting());

        Component kills = Component.translatable("text.fortress.most_kills",
                participants.get(mostKills).displayName,
                Component.literal(String.valueOf(participants.get(mostKills).kills))).withStyle(ChatFormatting.GREEN);

        Component captures = Component.translatable("text.fortress.most_captures",
                participants.get(mostCaptures).displayName,
                Component.literal(String.valueOf(participants.get(mostCaptures).captures))).withStyle(ChatFormatting.GREEN);

        PlayerSet players = gameSpace.getPlayers();
        players.showTitle(title, 1, 200, 3);
        players.sendMessage(Component.literal("------------------"));
        players.sendMessage(title);
        players.sendMessage(kills);
        players.sendMessage(captures);
        players.sendMessage(Component.literal("------------------"));
    }

    private EventResult onPlayerDeath(ServerPlayer playerEntity, DamageSource source) {
        Component deathMessage = getDeathMessage(playerEntity, source);
        gameSpace.getPlayers().sendMessage(deathMessage);
        getParticipant(playerEntity).deaths += 1;
        this.statistics.forPlayer(playerEntity).increment(StatisticKeys.DEATHS, 1);

        for (int i = 0; i < 75; i++) {
            level.sendParticles(
                    ParticleTypes.FIREWORK,
                    playerEntity.position().x(),
                    playerEntity.position().y() + 1.0f,
                    playerEntity.position().z(),
                    1,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    ((playerEntity.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                    0.1);
        }

        if (source.getEntity() != null && source.getEntity() instanceof ServerPlayer attacker) {
            FortressPlayer participant = getParticipant(attacker);

            if (participant != null) {
                participant.giveModule(attacker, participant.team, FortressModules.getRandomModule(attacker.getRandom()), 1);
                participant.kills += 1;
                this.statistics.forPlayer(attacker).increment(StatisticKeys.KILLS, 1);
            }
        }

        spawnDeadParticipant(playerEntity);
        return EventResult.DENY;
    }

    private Component getDeathMessage(ServerPlayer player, DamageSource source) {
        Component deathMes = source.getLocalizedDeathMessage(player);

        return Component.literal("☠ ").setStyle(Fortress.PREFIX_STYLE).append(deathMes.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf))));
    }

    private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        this.statistics.forPlayer(player).increment(StatisticKeys.DAMAGE_TAKEN, amount);

        if (source.getEntity() instanceof ServerPlayer attacker) {
            this.statistics.forPlayer(attacker).increment(StatisticKeys.DAMAGE_DEALT, amount);
        }

        return EventResult.PASS;
    }

    private void removePlayer(ServerPlayer playerEntity) {
        globalSidebar.removePlayer(playerEntity);
    }

    private void addPlayer(ServerPlayer playerEntity) {
        if (participants.containsKey(PlayerRef.of(playerEntity))) {
            playerEntity.getInventory().clearContent();

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

                playerEntity.getInventory().clearContent();
                spawnParticipant(playerEntity);
                fortressKit.giveItems(playerEntity, getParticipant(playerEntity).team);
            } else {
                FortressSpawnLogic.resetPlayer(playerEntity, GameType.SPECTATOR);
            }
        }
    }

    private EventResult onPlaceBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, UseOnContext context) {
        return EventResult.PASS;
    }

    private void onClose() {
        globalSidebar.hide();
    }

    private void onOpen() {
        for (Map.Entry<PlayerRef, FortressPlayer> entry : participants.entrySet()) {
            entry.getKey().ifOnline(level, this::spawnParticipant);
            entry.getValue().displayName = entry.getKey().getEntity(level).getDisplayName();
        }

        fortressKit.giveStarterKit(participants);

        stateManager.onOpen(level.getGameTime(), config);
    }

    public FortressPlayer getParticipant(ServerPlayer player) {
        return getParticipant(PlayerRef.of(player));
    }

    public FortressPlayer getParticipant(PlayerRef player) {
        return participants.get(player);
    }

    private void spawnDeadParticipant(ServerPlayer player) {
        player.setGameMode(GameType.SPECTATOR);

        FortressPlayer fortressPlayer = getParticipant(player);
        if (fortressPlayer != null) {
            fortressPlayer.timeOfDeath = level.getGameTime();
        }
    }

    private void spawnParticipant(ServerPlayer player) {
        FortressPlayer participant = getParticipant(player);
        assert participant != null;
        participant.timeOfSpawn = level.getGameTime();

        FortressSpawnLogic.resetPlayer(player, GameType.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.getSpawn(participant.team, player.getRandom()), level, participant.team == FortressTeams.RED.key() ? 180.0f : 0.0f);
    }

    public FortressMap getMap() {
        return map;
    }
}
