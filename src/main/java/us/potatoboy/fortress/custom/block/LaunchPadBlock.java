package us.potatoboy.fortress.custom.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.fake.FakeBlock;

public class LaunchPadBlock extends Block implements FakeBlock{
    //public static final int POWER_MIN = 1;
    //public static final int POWER_MAX = 8;
    //public static final IntProperty POWER = IntProperty.of("power", POWER_MIN, POWER_MAX);
    //protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 5.0, 16.0);

    private final Block proxy;

    public LaunchPadBlock(Block proxy) {
        super(FabricBlockSettings.of(Material.AIR).breakByHand(false).noCollision().dropsNothing());
        /*
        setDefaultState(this.getStateManager().getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(POWER, 3)
        );

         */

        this.proxy = proxy;
    }

    @Override
    public Block asProxy() {
        return this.proxy;
    }

    @Override
    public BlockState asProxy(BlockState state) {
        return this.asProxy().getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        /*
        builder.add(Properties.HORIZONTAL_FACING)
                .add(POWER);

         */
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        Vec3d velocity = entity.getRotationVector();
        velocity = velocity.multiply(1.5D, 0D, 1.5D);
        velocity = velocity.add(0D, 1.5D, 0D);

        entity.setVelocity(velocity);
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
        }
    }
}
