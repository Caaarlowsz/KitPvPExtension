package aw.rmjtromp.runicextension.kitpvp.core;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.RunicCore.utilities.builders.CustomItemBuilder;
import aw.rmjtromp.RunicCore.utilities.configs.Config;
import aw.rmjtromp.runicextension.kitpvp.core.kit.Kit;

public final class Kits implements Listener {

	private static final RunicCore plugin = RunicCore.getInstance();
	private HashMap<String, Kit> kits = new HashMap<>();
	
	public Kits loadKits(Config config) {
		if(kits.size() > 0) kits.clear();
		for(String kitName : config.getKeys()) {
			int slot = config.getInt(kitName+".display.slot", -1);
			if(config.getBoolean(kitName+".enabled") && (slot >= 0 && slot < 54)) {
				RunicItemStack item = new CustomItemBuilder(config, kitName+".display").getItem();
				Kit kit = new Kit(kitName, item);
				kit.getDisplayItem().setSlot(slot);
				
				// add kit potion effect
				if(config.contains(kitName+".effect.name")) {
					PotionEffectType effects = plugin.getLibrary().getPotionEffect(config.getString(kitName+".effect.name", "resistance"));
					int amplifier = config.getInt(kitName+".effect.amplifier", 0);
					if(effects != null && amplifier > 0 && amplifier < 257) {
						kit.setPotionEffect(effects, amplifier-1);
					}
				}
				
				// add kit contents
				if(config.contains(kitName+".contents")) {
					for(String content : config.getKeys(kitName+".contents")) {
						try {
							RunicItemStack i = new CustomItemBuilder(config, kitName+".contents."+content).getItem();
							if(config.isInteger(kitName+".contents."+content+".slot")) kit.getKitContents().addItem(config.getInt(kitName+".contents."+content+".slot", -1), i);
							else if(config.isString(kitName+".contents."+content+".slot")) kit.getKitContents().addItem(config.getString(kitName+".contents."+content+".slot"), i);
						} catch(Exception e) {}
					}
				}
				if(kits.containsKey(kitName)) kits.replace(kitName, kit);
				else kits.put(kitName, kit);
			}
		}
		System.out.print("[RunicCore] "+kits.size()+" "+(kits.size() > 1 ? "kits" : "kit")+" has been loaded");
		return this;
	}
	
	public Collection<Kit> getKits() {
		return kits.values();
	}
	
	public Kit getKit(String name) {
		for(String kitName : kits.keySet()) {
			if(kitName.equalsIgnoreCase(name)) return kits.get(kitName);
		}
		return null;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		KitPlayer player = KitPlayer.cast(e.getEntity());
		if(player.hasKit()) {
			player.clearInventory();
			player.setKit(null);
			player.spigot().respawn();
			player.heal();
		}
	}
	
}
