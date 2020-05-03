package aw.rmjtromp.runicextension.kitpvp.core;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.runicextension.kitpvp.core.events.PlayerKitChangeEvent;
import aw.rmjtromp.runicextension.kitpvp.core.kit.Kit;

public final class KitPlayer extends RunicPlayer implements Listener {

	private static HashMap<UUID, KitPlayer> players = new HashMap<>();
	
	public static KitPlayer cast(Object arg0) {
		if(arg0 instanceof KitPlayer) return (KitPlayer) arg0;
		else {
			RunicPlayer player = RunicPlayer.cast(arg0);
			if(player != null) {
				if(players.containsKey(player.getUniqueId())) return players.get(player.getUniqueId());
				else {
					players.put(player.getUniqueId(), new KitPlayer(player));
					return players.get(player.getUniqueId());
				}
			}
		}
		return null;
	}
	
	private KitPlayer(RunicPlayer player) {
		super(player.getPlayer());
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public boolean hasKit() {
		return hasMetadata("kit", plugin);
	}
	
	public void setKit(Kit kit) {
		if(hasKit()) removeMetadata("kit", plugin);
		if(kit != null) setMetadata("kit", new FixedMetadataValue(plugin, kit));
		Bukkit.getPluginManager().callEvent(new PlayerKitChangeEvent(this, kit));
	}
	
	public Kit getKit() {
		if(hasKit()) return (Kit) getMetadata("kit", plugin).value();
		return null;
	}
	
	public RunicPlayer getRunicPlayer() {
		return new RunicPlayer(this);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(this.equals(e.getPlayer())) {
			
//        	Unregister player after 10 seconds
    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
  				public void run() {
  					if(players.containsKey(e.getPlayer().getUniqueId())) {
  						if(!e.getPlayer().isOnline()) {
  	  						try { HandlerList.unregisterAll(players.get(e.getPlayer().getUniqueId())); } catch(Exception no) {}
  	  						players.remove(e.getPlayer().getUniqueId());
  						}
  					}
  				}
    		}, 10*20);
		}
	}

}
