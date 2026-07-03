package us.potatoboy.fortress.custom.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class TeslaCoilModuleItem extends ModuleItem {
    public TeslaCoilModuleItem(Item.Properties settings, Identifier structure) {
        super(settings, Items.LIGHTNING_ROD.waxed().unaffected(), structure);
    }

    @Override
    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerLevel level) {
        BlockBounds bounds = BlockBounds.of(center.offset(-4, 0, -4), center.offset(4, 4, 4));

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            ServerPlayer player = entry.getKey().getEntity(level);
            if (player == null) continue;
            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) continue;
            if (entry.getValue().team == owner) continue;
            if (!bounds.contains(player.blockPosition().getX(), player.blockPosition().getZ())) continue;

            player.hurtServer(level, player.damageSources().lightningBolt(), 1f);
            player.playSound(SoundEvents.TRIDENT_THUNDER.value(), 0.5f, 2f);
        }

        var random = level.getRandom();
        for (int i = 0; i < 10; i++) {

            Vec3 pos = HealModuleItem.randomPos(random, bounds);

            level.sendParticles(
                    ParticleTypes.WAX_ON,
                    pos.x() + 0.5,
                    pos.y() + 1,
                    pos.z() + 0.5,
                    1,
                    0.1, 0.1, 0.1,
                    15
            );
        }
    }
}
