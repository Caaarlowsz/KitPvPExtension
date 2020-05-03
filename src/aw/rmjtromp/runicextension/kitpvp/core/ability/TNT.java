package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class TNT extends Ability implements Listener {

	public TNT() {
		super(1);
	}

	@Override
	public boolean onAbility(KitPlayer player) {
		if(player.isInsideSafeRegion()) return false;
		
		player.playSound(player.getEyeLocation(), Sound.GHAST_FIREBALL, 1, 2);
		TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
		tnt.setVelocity(player.getEyeLocation().getDirection().multiply(1.5D));
		tnt.setMetadata("owner", new FixedMetadataValue(RunicCore.getInstance(), player.getRunicPlayer()));
		tnt.setFuseTicks(3*20);
		return true;
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed) e.getDamager();
			KitPlayer player = KitPlayer.cast(e.getEntity());
			if(tnt.hasMetadata("owner")) {
				if(!player.hasKit() || player.isInsideRegion("spawn") || player.isInsideSafeRegion()) e.setCancelled(true);
				else {
					for(MetadataValue mdv : tnt.getMetadata("owner")) {
						if(mdv.getOwningPlugin().equals(plugin)) {
							if(mdv instanceof RunicPlayer) {
								RunicPlayer owner = (RunicPlayer) mdv;
								if(owner.equals(player)) e.setCancelled(true);
								else {
									if(player.getHealth() - e.getFinalDamage() <= 0) {
										e.setCancelled(true);
										player.damage(e.getFinalDamage()*1.5, owner);
									}
								}
							}
							return;
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		if(e.getEntity() instanceof TNTPrimed) {
			if(e.getEntity().hasMetadata("owner")) {
				e.blockList().clear();
				if(isLocationInsideSafeRegion(e.getLocation())) e.setCancelled(true);
			}
		}
	}

}
