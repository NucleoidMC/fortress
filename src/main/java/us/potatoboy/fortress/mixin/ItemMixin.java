package us.potatoboy.fortress.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;
import us.potatoboy.fortress.event.UseItemOnBlockListener;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        if (!(playerEntity instanceof ServerPlayerEntity)) {
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(playerEntity.world);
        if (gameSpace != null) {
            try {
                UseItemOnBlockListener invoker = gameSpace.invoker(UseItemOnBlockListener.EVENT);
                ActionResult result = invoker.onItemUse((ServerPlayerEntity) playerEntity, context.getBlockPos(), context);
                if (result != ActionResult.PASS) {
                    cir.setReturnValue(result);
                }
            } catch (Throwable e) {
                Plasmid.LOGGER.error("An unexpected exception occurred when dispatching item on on block event", e);
                gameSpace.reportError(e, "Placing block");
            }
        }
    }
}
