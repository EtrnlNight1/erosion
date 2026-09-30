package co.bracesoftware.erosion.world.blocks.gas_desublimator;

import javax.annotation.Nullable;

import co.bracesoftware.erosion.ErosionExceptions.ErosionException;
import co.bracesoftware.erosion.network.server.ErosionNetworkSafeVariants.ErosionNetworkSafeBlockEntity;
import co.bracesoftware.erosion.world.ErosionRegistry;
import co.bracesoftware.erosion.world.custom.ErosionCustomEntitySys.GasType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class GasDesublimatorBlockEntity extends ErosionNetworkSafeBlockEntity<GasDesublimatorBlockEntity>
implements MenuProvider, Container
{
    private final NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
    public GasDesublimatorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ErosionRegistry.BlockEntities.GAS_DESUBLIMATOR.getBlockEntityHolder().get(), pos, state);
    }

    @Override public final int getContainerSize() { return this.contents.size(); }
    @Override public final ItemStack getItem(int s) { return this.contents.get(s); }
    @Override public final Component getDisplayName() { return Component.literal(ErosionRegistry.RawRegistry.GAS_DESUBLIMATOR.getName()); }
    @Override public final boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public final void clearContent() { this.contents.clear(); }
    @Override public final ItemStack removeItemNoUpdate(int s) { return ContainerHelper.takeItem(this.contents, s); }
    @Nullable @Override public final AbstractContainerMenu createMenu(int cid, Inventory pinv, Player p)
    {
        return ChestMenu.threeRows(cid, pinv, this);
    }

    @Override public final boolean isEmpty()
    {
        for(var is : this.contents)
        {
            if(!is.isEmpty()) return false;
        }
        return true;
    }

    @Override public final ItemStack removeItem(int s, int a)
    {
        var r = ContainerHelper.removeItem(this.contents, s, a);
        if(!r.isEmpty()) this.setChanged();
        return r;
    }

    @Override public final void setItem(int s, ItemStack is)
    {
        this.contents.set(s, is);
        if(is.getCount() > getMaxStackSize())
        {
            is.setCount(getMaxStackSize());
        }
        this.setChanged();
    }

    @Override protected final void saveAdditional(CompoundTag t, HolderLookup.Provider r)
    {
        super.saveAdditional(t, r);
        ContainerHelper.saveAllItems(t, this.contents, r);
        return;
    }

    @Override protected final void loadAdditional(CompoundTag t, HolderLookup.Provider r)
    {
        super.loadAdditional(t, r);
        ContainerHelper.loadAllItems(t, this.contents, r);
        return;
    }

    public static final boolean handleGasDesublimation(
        ServerLevel l, BlockPos bp, GasType gas
    )
    {
        if(l.getBlockEntity(bp) instanceof GasDesublimatorBlockEntity be)
        {
            var it = gas.getDesublimationProduct();
            if(it == null) return false;
            var what = new ItemStack(it, 1);
            var left = HopperBlockEntity.addItem(null, be, what, null);
            if(!left.isEmpty())
            {
                Containers.dropItemStack(
                    be.level,
                    be.worldPosition.getX(),
                    be.worldPosition.getY(),
                    be.worldPosition.getZ(),
                    left
                );
            }
            return true;
        }
        return false;
    }

    @Override public boolean onBlockEntityTickOnServer(
        GasDesublimatorBlockEntity e, ErosionBlockEntityTickPacket p
    ) throws ErosionException
    {
        return true;
    }
}
