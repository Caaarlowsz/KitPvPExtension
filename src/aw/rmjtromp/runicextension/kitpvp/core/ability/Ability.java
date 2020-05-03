package aw.rmjtromp.runicextension.kitpvp.core.ability;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.RunicCore.utilities.DependencyManager.Dependency;
import aw.rmjtromp.RunicCore.utilities.essential.Cooldown;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;
import aw.rmjtromp.runicextension.kitpvp.core.events.PlayerKitChangeEvent;

public abstract class Ability implements Listener {

	protected static final RunicCore plugin = RunicCore.getInstance();

	private Cooldown cooldown;
	
	private boolean cancelEvent = false;
	private boolean removeItemInHand = true;
	
	public boolean executeAbility(KitPlayer player) {
		if(!cooldown.containsPlayer(player)) {
			if(onAbility(player)) {
				cooldown.addPlayer(player);
				return true;
			}
		} else {
			long timeleft = cooldown.getTimeLeft(player.getRunicPlayer(), true);
			String time = "0 second";
			if(timeleft > 1000) {
				int a = Math.round(timeleft/1000);
				time = a > 1 ? a+" seconds" : a+" second";
			} else {
				long a = timeleft;
				time = a+"ms";
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You must wait &e"+time+" &7before you can use this ability again."));
		}
		return false;
	}
	
	public abstract boolean onAbility(KitPlayer player);
	
	public Ability(int cooldown) {
		this.cooldown = new Cooldown(this.toString()+"::ability", cooldown);
	}
	
	protected  final void setCancelled(boolean arg0) {
		cancelEvent = arg0;
	}
	
	public final boolean shoudCancelEvent() {
		return cancelEvent;
	}
	
	protected final void setRemoveItemOnUse(boolean arg0) {
		removeItemInHand = arg0;
	}
	
	public final boolean shouldRemoveItemOnUse() {
		return removeItemInHand;
	}
	
	protected final boolean isLocationInsideSafeRegion(Location loc) {
		if(Dependency.WORLDGUARD.isRegistered()) {
			WorldGuardPlugin worldGuard = (WorldGuardPlugin) Dependency.WORLDGUARD.getInstance();
			Map<String, ProtectedRegion> rgs = worldGuard.getRegionManager(loc.getWorld()).getRegions();
			for(ProtectedRegion region : rgs.values()) {
				if(region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
					Map<Flag<?>, Object> re = region.getFlags();
					for(Flag<?> f : re.keySet()) {
						Object value = re.get(f);
						if(value instanceof StateFlag.State) {
							StateFlag.State state = (StateFlag.State) value;
							if(f == DefaultFlag.PVP && state == StateFlag.State.DENY) return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerKitChange(PlayerKitChangeEvent e) {
		if(cooldown.containsPlayer(e.getPlayer().getRunicPlayer())) {
			cooldown.removePlayer(e.getPlayer().getRunicPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		RunicPlayer player = RunicPlayer.cast(e.getEntity());
		if(cooldown.containsPlayer(player)) {
			cooldown.removePlayer(player);
		}
	}
	
}
