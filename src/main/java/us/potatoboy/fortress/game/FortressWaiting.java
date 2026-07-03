package us.potatoboy.fortress.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import us.potatoboy.fortress.game.active.FortressActive;
import us.potatoboy.fortress.game.map.FortressMap;
import us.potatoboy.fortress.game.map.FortressMapGenerator;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Arrays;
import java.util.List;

public class FortressWaiting {
    private final GameSpace gameSpace;
    public final ServerLevel level;
    private final FortressMap map;
    private final FortressConfig config;
    private final TeamSelectionLobby teamSelectionLobby;

    private FortressWaiting(GameSpace gameSpace, ServerLevel level, FortressMap map, FortressConfig config, TeamSelectionLobby teamSelectionLobby) {
        this.gameSpace = gameSpace;
        this.level = level;
        this.map = map;
        this.config = config;
        this.teamSelectionLobby = teamSelectionLobby;
    }


    public static GameOpenProcedure open(GameOpenContext<FortressConfig> context) {
        FortressMapGenerator generator = new FortressMapGenerator(context.config().mapConfig());
        FortressMap map = generator.create(context.server());
        RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);

        return context.openWithLevel(levelConfig, (game, level) -> {
            GameWaitingLobby.addTo(game, context.config().playerConfig());

            GameTeamList teams = new GameTeamList(ImmutableList.of(FortressTeams.RED, FortressTeams.BLUE));
            TeamSelectionLobby teamSelectionLobby = TeamSelectionLobby.addTo(game, teams);

            FortressWaiting waiting = new FortressWaiting(game.getGameSpace(), level, map, context.config(), teamSelectionLobby);

            map.setStarterCells(FortressTeams.BLUE, "blue_start", level);
            map.setStarterCells(FortressTeams.RED, "red_start", level);

            game.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT, waiting::onBuildUiLayout);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(level, FortressSpawnLogic.choosePos(map.waitingSpawn, 0.0f)));
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(PlayerDeathEvent.EVENT, waiting::playerDeath);
            game.listen(ItemUseEvent.EVENT, waiting::onItemUse);
        });
    }

    private GameResult requestStart() {
        Multimap<GameTeamKey, ServerPlayer> players = HashMultimap.create();
        teamSelectionLobby.allocate(gameSpace.getPlayers(), players::put);

        FortressActive.open(gameSpace, level, map, config, players);

        return GameResult.ok();
    }

    private void addPlayer(ServerPlayer playerEntity) {
        spawnPlayer(playerEntity);
        giveBook(playerEntity);
    }

    private EventResult playerDeath(ServerPlayer playerEntity, DamageSource source) {
        playerEntity.setHealth(20.0F);
        spawnPlayer(playerEntity);
        return EventResult.PASS;
    }

    private void onBuildUiLayout(WaitingLobbyUiLayout layout, ServerPlayer player) {
//        SkyWarsWaiting waiting = this;
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Component> pages = Arrays.asList(
                Component.translatable("text.fortress.book.page1"),
                Component.translatable("text.fortress.book.page2")
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("How To Play"),
                "Potatoboy9999",
                0,
                pages.stream().map(Filterable::passThrough).toList(),
                false
        ));

        layout.addLeading(() -> GuiElementBuilder.from(book)
                .setCallback((index, type, action, gui) -> player.connection.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND)))
                .build()
        );
    }

    private InteractionResult onItemUse(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.is(Items.WRITTEN_BOOK)) {
            //if (WrittenBookItem.resolve(stack, player.getCommandSource(), player)) {
            //    player.currentScreenHandler.sendContentUpdates();
            //}

            player.connection.send(new ClientboundOpenBookPacket(hand));
        }

        return InteractionResult.SUCCESS;
    }

    private void spawnPlayer(ServerPlayer player) {
        FortressSpawnLogic.resetPlayer(player, GameType.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.waitingSpawn, level, 0.0f);
    }

    private void giveBook(ServerPlayer player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Component> pages = Arrays.asList(
                Component.translatable("text.fortress.book.page1"),
                Component.translatable("text.fortress.book.page2")
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("How To Play"),
                "Potatoboy9999",
                0,
                pages.stream().map(Filterable::passThrough).toList(),
                false
        ));

        player.getInventory().add(2, book);
    }
}
