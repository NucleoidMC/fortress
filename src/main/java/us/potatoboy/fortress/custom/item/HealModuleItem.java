package us.potatoboy.fortress.custom.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import us.potatoboy.fortress.game.active.FortressPlayer;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class HealModuleItem extends ModuleItem{
    public HealModuleItem(Item proxy, Identifier structure) {
        super(proxy, structure);
    }

    @Override
    public void tick(BlockPos center, Object2ObjectMap<PlayerRef, FortressPlayer> participants, GameTeam owner, ServerWorld world) {
        BlockBounds bounds = new BlockBounds(center.add(-1, 0, -1), center.add(1, 0, 1));

        for (Object2ObjectMap.Entry<PlayerRef, FortressPlayer> entry : Object2ObjectMaps.fastIterable(participants)) {
            ServerPlayerEntity player = entry.getKey().getEntity(world);
            if (player == null) continue;
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) continue;
            if (entry.getValue().team != owner) continue;
            if (!bounds.contains(player.getBlockPos().getX(), player.getBlockPos().getZ())) continue;

            StatusEffectInstance effectInstance = new StatusEffectInstance(StatusEffects.REGENERATION, 30, 1, true, true, true);
            player.addStatusEffect(effectInstance);
        }
    }
}
