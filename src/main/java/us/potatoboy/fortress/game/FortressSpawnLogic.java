package us.potatoboy.fortress.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;

public class FortressSpawnLogic {
    public static void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0f;
        player.clearStatusEffects();
        player.setFireTicks(0);
    }

    public static Vec3d choosePos(Random random, BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = MathHelper.nextDouble(random, min.getX(), max.getX());
        double z = MathHelper.nextDouble(random, min.getZ(), max.getZ());
        double y = min.getY() + aboveGround;

        return new Vec3d(x + 0.5, y, z + 0.5);
    }

    public static void spawnPlayer(ServerPlayerEntity player, BlockBounds bounds, ServerWorld world, float yaw) {
        Vec3d pos = choosePos(player.getRandom(), bounds, 0.5F);
        player.teleport(world, pos.x, pos.y, pos.z, yaw, 0.0F);
    }
}
