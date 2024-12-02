package us.potatoboy.fortress.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import us.potatoboy.fortress.game.active.FortressActive;
import us.potatoboy.fortress.game.map.FortressMap;
import us.potatoboy.fortress.game.map.FortressMapGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
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
    public final ServerWorld world;
    private final FortressMap map;
    private final FortressConfig config;
    private final TeamSelectionLobby teamSelectionLobby;

    private FortressWaiting(GameSpace gameSpace, ServerWorld world, FortressMap map, FortressConfig config, TeamSelectionLobby teamSelectionLobby) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;
        this.teamSelectionLobby = teamSelectionLobby;
    }


    public static GameOpenProcedure open(GameOpenContext<FortressConfig> context) {
        FortressMapGenerator generator = new FortressMapGenerator(context.config().mapConfig());
        FortressMap map = generator.create(context.server());

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setGameRule(GameRules.NATURAL_REGENERATION, false);

        return context.openWithWorld(worldConfig, (game, world) -> {
            GameWaitingLobby.addTo(game, context.config().playerConfig());

            GameTeamList teams = new GameTeamList(ImmutableList.of(FortressTeams.RED, FortressTeams.BLUE));
            TeamSelectionLobby teamSelectionLobby = TeamSelectionLobby.addTo(game, teams);

            FortressWaiting waiting = new FortressWaiting(game.getGameSpace(), world, map, context.config(), teamSelectionLobby);

            map.setStarterCells(FortressTeams.BLUE, "blue_start", world);
            map.setStarterCells(FortressTeams.RED, "red_start", world);

            game.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT, waiting::onBuildUiLayout);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(world, FortressSpawnLogic.choosePos(map.waitingSpawn, 0.0f)));
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(PlayerDeathEvent.EVENT, waiting::playerDeath);
            game.listen(ItemUseEvent.EVENT, waiting::onItemUse);
        });
    }

    private GameResult requestStart() {
        Multimap<GameTeamKey, ServerPlayerEntity> players = HashMultimap.create();
        teamSelectionLobby.allocate(gameSpace.getPlayers(), players::put);

        FortressActive.open(gameSpace, world, map, config, players);

        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity playerEntity) {
        spawnPlayer(playerEntity);
        giveBook(playerEntity);
    }

    private EventResult playerDeath(ServerPlayerEntity playerEntity, DamageSource source) {
        playerEntity.setHealth(20.0F);
        spawnPlayer(playerEntity);
        return EventResult.PASS;
    }

    private void onBuildUiLayout(WaitingLobbyUiLayout layout, ServerPlayerEntity player) {
//        SkyWarsWaiting waiting = this;
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Text> pages = Arrays.asList(
                Text.translatable("text.fortress.book.page1"),
                Text.translatable("text.fortress.book.page2")
        );

        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                RawFilteredPair.of("How To Play"),
                "Potatoboy9999",
                0,
                pages.stream().map(RawFilteredPair::of).toList(),
                false
        ));

        layout.addLeading(() -> GuiElementBuilder.from(book)
                .setCallback((index, type, action, gui) -> player.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(Hand.MAIN_HAND)))
                .build()
        );
    }

    private ActionResult onItemUse(ServerPlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (stack.isOf(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolve(stack, player.getCommandSource(), player)) {
                player.currentScreenHandler.sendContentUpdates();
            }

            player.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(hand));
        }

        return ActionResult.SUCCESS;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        FortressSpawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.waitingSpawn, world, 0.0f);
    }

    private void giveBook(ServerPlayerEntity player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Text> pages = Arrays.asList(
                Text.translatable("text.fortress.book.page1"),
                Text.translatable("text.fortress.book.page2")
        );

        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                RawFilteredPair.of("How To Play"),
                "Potatoboy9999",
                0,
                pages.stream().map(RawFilteredPair::of).toList(),
                false
        ));

        player.getInventory().insertStack(2, book);
    }
}
