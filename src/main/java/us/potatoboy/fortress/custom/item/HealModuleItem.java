package us.potatoboy.fortress.custom.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class HealModuleItem extends ModuleItem {
    public HealModuleItem(Item.Properties settings, Identifier structure) {
        super(settings, Items.STAINED_GLASS.pink(), structure);
    }

    @Override
    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeamKey owner, ServerLevel level) {
        BlockBounds bounds = BlockBounds.of(center.offset(-4, 0, -4), center.offset(4, 4, 4));

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            ServerPlayer player = entry.getKey().getEntity(level);
            if (player == null) continue;
            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) continue;
            if (entry.getValue().team != owner) continue;
            if (!bounds.contains(player.blockPosition().getX(), player.blockPosition().getZ())) continue;

            MobEffectInstance effectInstance = new MobEffectInstance(MobEffects.REGENERATION, 30, 1, true, true, true);
            player.addEffect(effectInstance);
        }

        var random = level.getRandom();
        DustParticleOptions effect = new DustParticleOptions(15105437, 2);
        for (int i = 0; i < 10; i++) {

            Vec3 pos = randomPos(random, bounds);

            level.sendParticles(
                    effect,
                    pos.x() + 0.5,
                    pos.y() + 1,
                    pos.z() + 0.5,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    public static Vec3 randomPos(RandomSource random, BlockBounds bounds) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = Mth.nextDouble(random, min.getX(), max.getX());
        double z = Mth.nextDouble(random, min.getZ(), max.getZ());
        double y = Mth.nextDouble(random, min.getY(), max.getY());

        return new Vec3(x, y, z);
    }
}
