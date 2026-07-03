package us.potatoboy.fortress.game;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.Set;

public class FortressSpawnLogic {
    private static final RandomSource random = RandomSource.createThreadLocalInstance();

    public static void resetPlayer(ServerPlayer player, GameType gameMode) {
        player.setGameMode(gameMode);
        player.setDeltaMovement(Vec3.ZERO);
        player.getFoodData().setFoodLevel(20);
        player.fallDistance = 0.0f;
        player.removeAllEffects();
        player.setRemainingFireTicks(0);
    }

    public static Vec3 choosePos(BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = Mth.nextDouble(random, min.getX(), max.getX());
        double z = Mth.nextDouble(random, min.getZ(), max.getZ());
        double y = min.getY() + aboveGround;

        return new Vec3(x + 0.5, y, z + 0.5);
    }

    public static void spawnPlayer(ServerPlayer player, BlockBounds bounds, ServerLevel level, float yaw) {
        Vec3 pos = choosePos(bounds, 0.5F);
        player.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), 0, 0, false);
    }
}
