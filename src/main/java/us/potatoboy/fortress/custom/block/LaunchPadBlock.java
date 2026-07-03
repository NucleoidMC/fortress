package us.potatoboy.fortress.custom.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class LaunchPadBlock extends Block implements PolymerBlock {
    public LaunchPadBlock(BlockBehaviour.Properties settings) {
        super(settings);

    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.onGround()) {
            Vec3 velocity = entity.getLookAngle();
            velocity = velocity.multiply(1.5D, 0D, 1.5D);
            velocity = velocity.add(0D, 1.5D, 0D);

            entity.setDeltaMovement(velocity);
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.SLIME_BLOCK_PLACE), SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.5f, 1, level.getRandom().nextLong()));
            }
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.SLIME_BLOCK.defaultBlockState();
    }
}
