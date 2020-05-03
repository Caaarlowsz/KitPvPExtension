package aw.rmjtromp.runicextension.kitpvp.core;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.runicextension.kitpvp.core.ability.Ability;
import aw.rmjtromp.runicextension.kitpvp.core.ability.PvP;
import aw.rmjtromp.runicextension.kitpvp.core.ability.Fisherman;
import aw.rmjtromp.runicextension.kitpvp.core.ability.Archer;
import aw.rmjtromp.runicextension.kitpvp.core.ability.Snow;
import aw.rmjtromp.runicextension.kitpvp.core.ability.TNT;
import aw.rmjtromp.runicextension.kitpvp.core.ability.Tank;

public final class Abilities implements Listener {

	private static HashMap<String, Ability> abilities = new HashMap<>();
	
	private static enum ABILITY {
		Default(new PvP()),
		TNTThrower(new TNT()),
		MachineGun(new Archer()),
		Fisherman(new Fisherman()),
		Snow(new Snow()),
		Tank(new Tank());
		
		private Ability ability;
		ABILITY(Ability ability) {
			this.ability = ability;
		}
		
		public Ability getAbility() {
			return ability;
		}
	}
	
	private Abilities() {
		Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
		registerAbility(ABILITY.Default);
		registerAbility(ABILITY.TNTThrower);
		registerAbility(ABILITY.MachineGun);
		registerAbility(ABILITY.Fisherman);
		registerAbility(ABILITY.Snow);
		registerAbility(ABILITY.Tank);
	}
	
	public static Abilities init() {
		return new Abilities();
	}
	
	private void registerAbility(ABILITY arg0) {
		if(abilities.containsKey(arg0.toString().toLowerCase())) {
			if(abilities.get(arg0.toString().toLowerCase()) instanceof Listener) {
				HandlerList.unregisterAll((Listener) abilities.get(arg0.toString().toLowerCase()));
			}
			abilities.replace(arg0.toString().toLowerCase(), arg0.getAbility());
		} else abilities.put(arg0.toString().toLowerCase(), arg0.getAbility());
		
		if(arg0.getAbility() instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) arg0.getAbility(), RunicCore.getInstance());
		}
	}
	
	private boolean hasAbility(ABILITY arg0) {
		return abilities.containsKey(arg0.toString().toLowerCase());
	}
	
	@SuppressWarnings("unused")
	private void unregisterAbility(ABILITY arg0) {
		if(hasAbility(arg0)) {
			if(abilities.get(arg0.toString().toLowerCase()) instanceof Listener) {
				HandlerList.unregisterAll((Listener) abilities.get(arg0.toString().toLowerCase()));
			}
			abilities.remove(arg0.toString().toLowerCase());
		}
	}
	
	public static Set<String> getRegisteredAbilities() {
		return abilities.keySet();
	}
	
	@EventHandler
	public void onPlayerIteract(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			KitPlayer player = KitPlayer.cast(e.getPlayer());
			if(player.hasKit()) {
				if(player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
					RunicItemStack item = new RunicItemStack(player.getItemInHand());
					if(item.hasNBTTag("ability") && abilities.containsKey(item.getNBTTagAsString("ability").toLowerCase())) {
						Ability ability = abilities.get(item.getNBTTagAsString("ability").toLowerCase());
						if(ability.executeAbility(player)) {
							e.setCancelled(ability.shoudCancelEvent());
							if(ability.shoudCancelEvent()) {
								player.sendMessage("&cPlayerInteractEvent cancelled &7by Abilities.java:101&c because item has 'ability' tag.");
							}
							if(ability.shouldRemoveItemOnUse()) {
								if(item.getAmount() > 1) {
									int newAmount = item.getAmount() - 1;
									player.getItemInHand().setAmount(newAmount);
								}
								else player.setItemInHand(null);
								player.updateInventory();
							}
						} else {
							player.sendMessage("&cPlayerInteractEvent cancelled &7by Abilities.java:115&c because item has 'ability' tag.");
							e.setCancelled(true);
						}
					} else if(item.hasNBTTag("ability")) {
						e.setCancelled(true);
						player.sendMessage("&cPlayerInteractEvent cancelled &7by Abilities.java:118&c because item has 'ability' tag.");
						player.sendMessage("&cThis ability has not yet been completed.");
					}
				}
			}
		}
	}
	
}
