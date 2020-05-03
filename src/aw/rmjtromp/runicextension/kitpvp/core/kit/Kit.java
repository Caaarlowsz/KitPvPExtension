package aw.rmjtromp.runicextension.kitpvp.core.kit;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.RunicCore.utilities.placeholders.Placeholder;
import aw.rmjtromp.runicextension.kitpvp.KitPvP;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Kit {
	
	private String name;
	
	private KitDisplayItem displayItem;
	private KitContents contents;
	private PotionEffect potioneffect;

	public Kit(String name, ItemStack displayItem) {
		this.name = name;
		this.displayItem = new KitDisplayItem(new RunicItemStack(displayItem).setNBTTag("selectKit", name));
		this.contents = new KitContents();
	}
	
	public String getName() {
		return name;
	}
	
	public KitDisplayItem getDisplayItem() {
		return displayItem;
	}
	
	public KitContents getKitContents() {
		return contents;
	}
	
	public PotionEffect getPotionEffect() {
		return potioneffect;
	}
	
	public Kit setPotionEffect(PotionEffectType type, int amplifier) {
		potioneffect = new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false);
		return this;
	}
	
	public Kit equipPlayer(KitPlayer player) {
		player.clearInventory();
		player.removeActivePotionEffects();
		player.setHealth(player.getMaxHealth());
		player.feed();
		player.setKit(this);
		
		for(Object slot : getKitContents().getContents().keySet()) {
			if(slot instanceof String) {
				String s = (String) slot;
				if(s.matches("helmet")) player.getInventory().setHelmet(getKitContents().getContent(s));
				else if(s.matches("chest(plate)?")) player.getInventory().setChestplate(getKitContents().getContent(s));
				else if(s.matches("leggings?")) player.getInventory().setLeggings(getKitContents().getContent(s));
				else if(s.matches("boots?")) player.getInventory().setBoots(getKitContents().getContent(s));
			} else if(slot instanceof Integer) {
				int s = (Integer) slot;
				player.getInventory().setItem(s, getKitContents().getContent(s));
			}
		}
		
		// give player kit's potion effect, update inventory and send player message
		if(potioneffect != null) player.addPotionEffect(potioneffect);
		player.updateInventory();
		player.sendMessage(Placeholder.parse(KitPvP.getInstance().kitpvp_kit_equipped, player).set("{KIT}", getName()).getString());
		player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 1);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		else if(obj instanceof Kit) return name.equals(((Kit)obj).getName());
		return false;
	}
	
	@Override
	public String toString() {
		return "Kit::"+name;
	}
	
}
