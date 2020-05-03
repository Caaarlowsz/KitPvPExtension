package aw.rmjtromp.runicextension.kitpvp.core.ability;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Snow extends Ability implements Listener {

	public Snow() {
		super(1);
	}
	
	@Override
	public boolean onAbility(KitPlayer player) {
		if(player.isInsideSafeRegion()) return false;
		
		setCancelled(false);
		setRemoveItemOnUse(false);
		return true;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if(e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof Snowball) {
			if(!isLocationInsideSafeRegion(e.getEntity().getLocation())) {
				KitPlayer owner = KitPlayer.cast(e.getEntity().getShooter());
				List<Entity> entities = e.getEntity().getNearbyEntities(5, 5, 5);
				for(Entity entity : entities) {
					if(!(entity instanceof Player)) continue;
					KitPlayer player = KitPlayer.cast((Player) entity);
					if(!player.equals(e.getEntity().getShooter()) && !player.isInsideSafeRegion()) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*5, 255, true, false));
						if(player.hasKit() && player.getKit().getName().equalsIgnoreCase("blaze")) {
							player.damage(0.5, owner.getPlayer());
						}
					}
				}
			}
		}
	}

}
