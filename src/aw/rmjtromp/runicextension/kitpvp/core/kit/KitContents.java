package aw.rmjtromp.runicextension.kitpvp.core.kit;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;

public final class KitContents {

	private HashMap<Object, RunicItemStack> contents = new HashMap<>();
	
	public KitContents addItem(String slot, ItemStack item) {
		if(slot.toLowerCase().matches("^([0-9]{1,2}|helmet|chest(plate)?|leggings?|boots?)$")) {
			if(slot.matches("^[0-9]+$")) return addItem(Integer.parseInt(slot), item);
			else if(slot.toLowerCase().matches("^(helmet|chest(plate)?|leggings?|boots?)$")) {
				if(contents.containsKey(slot.toLowerCase())) contents.replace(slot, (item instanceof RunicItemStack ? (RunicItemStack) item : new RunicItemStack(item)));
				else contents.put(slot.toLowerCase(), (item instanceof RunicItemStack ? (RunicItemStack) item : new RunicItemStack(item)));
			}
		}
		return this;
	}
	
	public KitContents addItem(int slot, ItemStack item) {
		if(slot >= 0 && slot < 54) {
			if(contents.containsKey(slot)) contents.replace(slot+"", (item instanceof RunicItemStack ? (RunicItemStack) item : new RunicItemStack(item)));
			else contents.put(slot, (item instanceof RunicItemStack ? (RunicItemStack) item : new RunicItemStack(item)));
		}
		return this;
	}
	
	public HashMap<Object, RunicItemStack> getContents() {
		return contents;
	}
	
	public RunicItemStack getContent(Object slot) {
		if(slot instanceof String) {
			String s = ((String)slot).toLowerCase();
			if(s.matches("^([0-9]{1,2}|helmet|chest(plate)?|leggings?|boots?)$")) {
				if(s.matches("^[0-9]+$")) return getContents().containsKey(Integer.parseInt((String) slot)) ? getContents().get(Integer.parseInt((String) slot)) : null;
				else if(s.matches("^(helmet|chest(plate)?|leggings?|boots?)$")) return getContents().containsKey(s) ? getContents().get(s) : null;
			}
		} else if(slot instanceof Integer) {
			int s = (Integer) slot;
			return getContents().containsKey(s) ? getContents().get(s) : null;
		}
		return null;
	}
	
}
