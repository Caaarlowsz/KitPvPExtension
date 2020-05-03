package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class End extends Ability {
	
	public End() {
		super(3);
	}
	
	// TODO make sure that vanished staff members are not included

	@Override
	public boolean onAbility(KitPlayer player) {
		setCancelled(true);
		if(Bukkit.getOnlinePlayers().size() > 1) {
			if(player.getWorld().getPlayers().size() > 1) {
				Player nearestPlayer = getNearestPlayer(player);
				if(nearestPlayer != null) {
					setRemoveItemOnUse(true);
					player.teleport(nearestPlayer);
					player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
					return true;
				} else player.sendMessage("&7There are no players nearby with a kit.");
			} else player.sendMessage("&7There are no players nearby to teleport to.");
		} else player.sendMessage("&7There are no other players online to teleport to.");
		return false;
	}
	
	private Player getNearestPlayer(Player player) {
		Player NearestPlayer = null;
		double lastDistance = Integer.MAX_VALUE;
		
		for(Player p : player.getWorld().getPlayers()) {
			KitPlayer kp = KitPlayer.cast(p);
			if(!kp.hasKit() || kp.isAFK() || kp.isInsideSafeRegion() || kp.isVanished()) continue;
			double distance = player.getLocation().distance(p.getLocation());
			if(distance < lastDistance) {
				lastDistance = distance;
				NearestPlayer = p;
			}
		}
		return NearestPlayer;
	}

}
