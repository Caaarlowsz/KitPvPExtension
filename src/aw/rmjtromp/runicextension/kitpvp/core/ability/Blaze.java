package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Blaze extends Ability implements Listener {
	
	public Blaze() {
		super(3);
	}

	@Override
	public boolean onAbility(KitPlayer player) {
		if(!isLocationInsideSafeRegion(player.lookingAt())) {
			player.getWorld().strikeLightning(player.lookingAt());
			return true;
		}
		
		player.sendMessage("&cYou can't strike a lightning at protected areas.");
		return false;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		if(player.hasKit() && player.getKit().getName().equalsIgnoreCase("blaze")) {
			if(player.getLocation().getBlock().getType().equals(Material.WATER) || player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)
					|| player.getEyeLocation().getBlock().getType().equals(Material.WATER) || player.getEyeLocation().getBlock().getType().equals(Material.STATIONARY_WATER)) {
				player.damage(0.5);
			}
		}
	}

}
