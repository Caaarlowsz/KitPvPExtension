package aw.rmjtromp.runicextension.kitpvp.core.kit;

import org.bukkit.inventory.ItemStack;

import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;

public final class KitDisplayItem {

	private RunicItemStack item;
	private int slot;
	
	public KitDisplayItem(ItemStack item) {
		this.item = new RunicItemStack(item);
	}
	
	public RunicItemStack getDisplayItem() {
		return item;
	}
	
	public KitDisplayItem setSlot(int slot) {
		this.slot = slot;
		return this;
	}
	
	public int getSlot() {
		return slot;
	}
	
}
