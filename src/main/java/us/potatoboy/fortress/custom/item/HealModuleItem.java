package us.potatoboy.fortress.custom.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import us.potatoboy.fortress.game.FortressSpawnLogic;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.Random;

public class HealModuleItem extends ModuleItem{
    public HealModuleItem(Item proxy, Identifier structure) {
        super(proxy, structure);
    }

    @Override
    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeam owner, ServerWorld world) {
        BlockBounds bounds = new BlockBounds(center.add(-4, 0, -4), center.add(4, 4, 4));

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            ServerPlayerEntity player = entry.getKey().getEntity(world);
            if (player == null) continue;
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) continue;
            if (entry.getValue().team != owner) continue;
            if (!bounds.contains(player.getBlockPos().getX(), player.getBlockPos().getZ())) continue;

            StatusEffectInstance effectInstance = new StatusEffectInstance(StatusEffects.REGENERATION, 30, 1, true, true, true);
            player.addStatusEffect(effectInstance);
        }

        Random random = new Random();
        DustParticleEffect effect = new DustParticleEffect(0.99f, 0.49f, 0.61f, 2);
        for (int i = 0; i < 10; i++) {

            Vec3d pos = randomPos(random, bounds);

            world.spawnParticles(
                    effect,
                    pos.getX() + 0.5,
                    pos.getY() + 1,
                    pos.getZ() + 0.5,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    public static Vec3d randomPos(Random random, BlockBounds bounds) {
        BlockPos min = bounds.getMin();
        BlockPos max = bounds.getMax();

        double x = MathHelper.nextDouble(random, min.getX(), max.getX());
        double z = MathHelper.nextDouble(random, min.getZ(), max.getZ());
        double y = MathHelper.nextDouble(random, min.getY(), max.getY());

        return new Vec3d(x, y, z);
    }
}
