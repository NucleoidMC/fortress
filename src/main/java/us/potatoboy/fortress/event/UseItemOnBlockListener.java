package us.potatoboy.fortress.event;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.event.EventType;

public interface UseItemOnBlockListener {
    EventType<UseItemOnBlockListener> EVENT = EventType.create(UseItemOnBlockListener.class, listeners -> ((player, pos, itemUsageContext) -> {
        for (UseItemOnBlockListener listener : listeners) {
            ActionResult result = listener.onItemUse(player, pos, itemUsageContext);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    }));

    ActionResult onItemUse(ServerPlayerEntity player, BlockPos pos, ItemUsageContext itemUsageContext);
}
