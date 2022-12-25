package us.potatoboy.fortress.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import us.potatoboy.fortress.game.active.FortressActive;
import us.potatoboy.fortress.game.map.FortressMap;
import us.potatoboy.fortress.game.map.FortressMapGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

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

            game.allow(GameRuleType.USE_ITEMS);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, FortressSpawnLogic.choosePos(world.random, map.waitingSpawn, 0.0f)));
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

    private ActionResult playerDeath(ServerPlayerEntity playerEntity, DamageSource source) {
        playerEntity.setHealth(20.0F);
        spawnPlayer(playerEntity);
        return ActionResult.PASS;
    }

    private TypedActionResult<ItemStack> onItemUse(ServerPlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (stack.isOf(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolve(stack, player.getCommandSource(), player)) {
                player.currentScreenHandler.sendContentUpdates();
            }

            player.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(hand));
        }

        return TypedActionResult.success(stack, true);
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        FortressSpawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        FortressSpawnLogic.spawnPlayer(player, map.waitingSpawn, world, 0.0f);
    }

    private void giveBook(ServerPlayerEntity player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        NbtList pages = new NbtList();

        pages.add(NbtString.of(Text.Serializer.toJson(Text.translatable("text.fortress.book.page1"))));
        pages.add(NbtString.of(Text.Serializer.toJson(Text.translatable("text.fortress.book.page2"))));

        book.getOrCreateNbt().put("pages", pages);
        book.getOrCreateNbt().putString("title", "How To Play");
        book.getOrCreateNbt().putString("author", "Potatoboy9999");
        book.getOrCreateNbt().putInt("HideFlags", 63);
        book.getOrCreateNbt().putBoolean("resolved", false);

        player.getInventory().insertStack(2, book);
    }
}
