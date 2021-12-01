/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [10/01/2016, 15:13:46 (GMT)]
 */
package vazkii.arl.block.tile;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class TileSimpleInventory extends TileMod implements ISidedInventory {

	public TileSimpleInventory(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	protected NonNullList<ItemStack> inventorySlots = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
	
	@Override
	public void readSharedNBT(CompoundNBT par1NBTTagCompound) {
		if(!needsToSyncInventory())
			return;
		
		ListNBT var2 = par1NBTTagCompound.getList("Items", 10);
		clearContent();
		for(int var3 = 0; var3 < var2.size(); ++var3) {
			CompoundNBT var4 = var2.getCompound(var3);
			byte var5 = var4.getByte("Slot");
			if (var5 >= 0 && var5 < inventorySlots.size())
				inventorySlots.set(var5, ItemStack.of(var4));
		}
	}

	@Override
	public void writeSharedNBT(CompoundNBT par1NBTTagCompound) {
		if(!needsToSyncInventory())
			return;
		
		ListNBT var2 = new ListNBT();
		for (int var3 = 0; var3 < inventorySlots.size(); ++var3) {
			if(!inventorySlots.get(var3).isEmpty()) {
				CompoundNBT var4 = new CompoundNBT();
				var4.putByte("Slot", (byte)var3);
				inventorySlots.get(var3).save(var4);
				var2.add(var4);
			}
		}
		par1NBTTagCompound.put("Items", var2);
	}
	
	protected boolean needsToSyncInventory() {
		return true;
	}
	
	@Nonnull
	@Override
	public ItemStack getItem(int i) {
		return inventorySlots.get(i);
	}

	@Nonnull
	@Override
	public ItemStack removeItem(int i, int j) {
		if (!inventorySlots.get(i).isEmpty()) {
			ItemStack stackAt;

			if (inventorySlots.get(i).getCount() <= j) {
				stackAt = inventorySlots.get(i);
				inventorySlots.set(i, ItemStack.EMPTY);
				inventoryChanged(i);
				return stackAt;
			} else {
				stackAt = inventorySlots.get(i).split(j);

				if (inventorySlots.get(i).getCount() == 0)
					inventorySlots.set(i, ItemStack.EMPTY);
				inventoryChanged(i);

				return stackAt;
			}
		}

		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int i) {
		ItemStack stack = getItem(i);
		setItem(i, ItemStack.EMPTY);
		inventoryChanged(i);
		return stack;
	}

	@Override
	public void setItem(int i, @Nonnull ItemStack itemstack) {
		inventorySlots.set(i, itemstack);
		inventoryChanged(i);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}
	
	@Override
	public boolean isEmpty() {
		for(int i = 0; i < getContainerSize(); i++) {
			ItemStack stack = getItem(i);
			if(!stack.isEmpty())
				return false;
		}
			
		return true;
	}

	@Override
	public boolean stillValid(@Nonnull PlayerEntity entityplayer) {
		return getLevel().getBlockEntity(getBlockPos()) == this && entityplayer.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (LazyOptional<T>) LazyOptional.of(() -> new SidedInvWrapper(this, facing));
		
		return LazyOptional.empty();
	}

	@Override
	public boolean canPlaceItem(int i, @Nonnull ItemStack itemstack) {
		return true;
	}

	@Override
	public void startOpen(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public void stopOpen(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public void clearContent() {
		inventorySlots = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
	}

	public void inventoryChanged(int i) {
		// NO-OP
	}

	public boolean isAutomationEnabled() {
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction direction) {
		return isAutomationEnabled();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack itemStackIn, @Nonnull Direction direction) {
		return isAutomationEnabled();
	}

	@Nonnull
	@Override
	public int[] getSlotsForFace(@Nonnull Direction side) {
		if(isAutomationEnabled()) {
			int[] slots = new int[getContainerSize()];
			for(int i = 0; i < slots.length; i++)
				slots[i] = i;
			return slots;
		}

		return new int[0];
	}
}
