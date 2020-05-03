package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Fisherman extends Ability {
	
	public Fisherman() {
		super(3);
	}

	@Override
	public boolean onAbility(KitPlayer player) {
		player.playSound(player.getEyeLocation(), Sound.FIZZ, 1, 1);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3*20, 1, true, false));
		return true;
	}
	
	@EventHandler
	public void onPlayerFish(PlayerFishEvent e) {
		e.setExpToDrop(0);
		
		if(e.getCaught() != null) {
			KitPlayer player = KitPlayer.cast(e.getPlayer());
			if(player.hasKit() && player.getKit().getName().equalsIgnoreCase("fisherman")) {
				Entity entity = e.getCaught();
				if(!isLocationInsideSafeRegion(entity.getLocation())) pullEntityToLocation(entity, e.getPlayer().getLocation());
				else e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		if(player.hasKit() && player.getKit().getName().equalsIgnoreCase("fisherman")) {
			if(player.getEyeLocation().getBlock().getType().equals(Material.WATER) || e.getPlayer().getEyeLocation().getBlock().getType().equals(Material.STATIONARY_WATER)) {
				if(!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1, true, false));
			} else {
				if(player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) e.getPlayer().removePotionEffect(PotionEffectType.WATER_BREATHING);
			}
		}
	}
	
	private void pullEntityToLocation(Entity e, Location loc) {
	    Location entityLoc = e.getLocation();
	    entityLoc.setY(entityLoc.getY() + 0.5D);
	    e.teleport(entityLoc);
	    double g = -0.08D;
	    double t = loc.distance(entityLoc);
	    double v_x = (1.0D + 0.07D * t) * (loc.getX() - entityLoc.getX()) / t;
	    double v_y = (1.0D + 0.03D * t) * (loc.getY() - entityLoc.getY()) / t - 0.5D * g * t;
	    double v_z = (1.0D + 0.07D * t) * (loc.getZ() - entityLoc.getZ()) / t;
	    Vector v = e.getVelocity();
	    v.setX(v_x);
	    v.setY(v_y);
	    v.setZ(v_z);
	    e.setVelocity(v);
	}

}
