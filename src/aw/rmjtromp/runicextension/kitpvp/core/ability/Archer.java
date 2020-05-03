package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.utilities.essential.Cooldown;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Archer extends Ability implements Listener {
	
	public Archer() {
		super(3);
		cooldown = new Cooldown("ArcherAbility", 10);
	}
	
	private Cooldown cooldown;

	@Override
	public boolean onAbility(KitPlayer player) {
		if(!cooldown.containsPlayer(player.getRunicPlayer())) {
			if(!player.isInsideSafeRegion()) {
				player.playSound(player.getEyeLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
				cooldown.addPlayer(player.getRunicPlayer());
				player.sendMessage("&7Machine gun ability activated.");
				return true;
			} else {
				player.sendMessage("&cYou can't use that here.");
				return false;
			}
		}
		
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou already have an active machine gun."));
		return false;
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent e) {
		if(e.getEntity() instanceof Player) {
			KitPlayer player = KitPlayer.cast(e.getEntity());
			if(player.hasKit() && player.getKit().getName().equalsIgnoreCase("archer")) {
				if(cooldown.containsPlayer(player.getRunicPlayer())) {
					if(!player.isInsideSafeRegion()) {
						e.setCancelled(true);
						cooldown.removePlayer(player.getRunicPlayer());
						new BukkitRunnable() {
							int ticks = 0;
							@Override
							public void run() {
								ticks++;
								if(ticks > 20 || !player.getItemInHand().getType().equals(Material.BOW)) cancel();
								Location frontlocation = player.getEyeLocation().add(player.getLocation().getDirection());
								
								Arrow arrow = (Arrow) player.getWorld().spawnEntity(frontlocation, EntityType.ARROW);
								arrow.setVelocity(frontlocation.getDirection().multiply(3));
								arrow.setShooter(player.getPlayer());
								
								player.getWorld().playSound(frontlocation, Sound.FIREWORK_LARGE_BLAST, 1.5F, 0.5F);
			                    player.getWorld().playEffect(frontlocation, Effect.SMOKE, 10, 10);
			                    

								RunicCore.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), new Runnable() {
									  public void run() {
										  arrow.remove();
									  }
								}, 3*20);
							}
						}.runTaskTimer(RunicCore.getInstance(), 0, 2);
					} else {
						e.setCancelled(true);
						player.sendMessage("&cYou can't use that here.");
					}
				}
			}
		}
	}

}
