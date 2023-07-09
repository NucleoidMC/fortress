package us.potatoboy.fortress.custom.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LaunchPadBlock extends Block implements PolymerBlock {
    public LaunchPadBlock() {
        super(FabricBlockSettings.create().noCollision().dropsNothing());

    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (entity.isOnGround()) {
            Vec3d velocity = entity.getRotationVector();
            velocity = velocity.multiply(1.5D, 0D, 1.5D);
            velocity = velocity.add(0D, 1.5D, 0D);

            entity.setVelocity(velocity);
            if (entity instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
                player.playSound(SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS, 0.5f, 1);
            }
        }
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.SLIME_BLOCK;
    }
}
