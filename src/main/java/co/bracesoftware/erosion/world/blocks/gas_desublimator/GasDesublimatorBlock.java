package co.bracesoftware.erosion.world.blocks.gas_desublimator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import co.bracesoftware.erosion.ErosionClient.ErosionScreenMessage;
import co.bracesoftware.erosion.ErosionUtils;
import co.bracesoftware.erosion.network.server.ErosionNetworkSafeVariants.ErosionNetworkSafeBaseEntityBlock;
import co.bracesoftware.erosion.network.server.ErosionNetworkSafeVariants.ErosionNetworkSafeBlockEntity;
import co.bracesoftware.erosion.world.ErosionRegistry;
import co.bracesoftware.erosion.world.blocks.chemical_reactor.ChemicalReactorSystemCore.IErosionChemicalReactorMultiBlockComponent;

public final class GasDesublimatorBlock extends ErosionNetworkSafeBaseEntityBlock<GasDesublimatorBlock>
implements IErosionChemicalReactorMultiBlockComponent
{
    public static final VoxelShape SHAPE = Shapes.or(
        ErosionUtils.ModelRendering.createBoxCentered(3, 16, 16, 0),
        ErosionUtils.ModelRendering.createBoxCentered(16, 12, 12, 0)
    );
    
    public GasDesublimatorBlock(Block.Properties p)
    {
        super(p,
            () -> (
                BlockEntityType<? extends ErosionNetworkSafeBlockEntity<?>>
            ) ErosionRegistry.BlockEntities.GAS_DESUBLIMATOR.getBlockEntityHolder().get(),
            GasDesublimatorBlock::new
        );

        this.setServerLogic(new GasDesublimatorBlockServerLogic());
    }

    @Nullable 
    @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s)
    {
        return new GasDesublimatorBlockEntity(p,s);
    }

    public static final class GasDesublimatorBlockServerLogic extends ErosionNetworkSafeBlockSidedLogic
    {
        @Override public boolean useWithoutItem(ErosionBlockInteractionPacket p)
        {
            var be = p.getServerLevel().getBlockEntity(p.getBlockPos());
            if(be instanceof GasDesublimatorBlockEntity gds)
            {
                p.getServerPlayer().openMenu(gds);
            }
            return true;
        }
        @Override public void onInteractionFail(ErosionBlockInteractionPacket p)
        {
            ErosionUtils.displayMessage(
                p.getServerPlayer(), "Cannot do that",
                ErosionScreenMessage.Color.DARK_RED
            );
            return;
        }
    }

    @Override protected final void onRemove(
        BlockState bs, Level l, BlockPos bp,
        BlockState nbs, boolean piston
    )
    {
        if(!bs.is(nbs.getBlock()))
        {
            var be = l.getBlockEntity(bp);
            if(be instanceof GasDesublimatorBlockEntity gds)
            {
                Containers.dropContents(l, bp, gds);
                l.updateNeighbourForOutputSignal(bp, this);
            }
            super.onRemove(bs, l, bp, nbs, piston);
        }
        return;
    }

    @Override 
    public VoxelShape getShape(
        BlockState bs,
        BlockGetter bg,
        BlockPos bp,
        CollisionContext c
    )
    {
        return SHAPE;
    }
}