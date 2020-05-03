package aw.rmjtromp.runicextension.kitpvp.core.animation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.utilities.DependencyManager;
import aw.rmjtromp.RunicCore.utilities.DependencyManager.Dependency;
import aw.rmjtromp.RunicCore.utilities.placeholders.Placeholder;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

public final class DeathAnimation implements Listener {
	
	private List<KitPlayer> deads = new ArrayList<>();
	
	public DeathAnimation() {
		// makes spectators in tablist look normal (instead of italic and gray)
		if(Dependency.PLACEHOLDERAPI.isRegistered()) {
			ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicCore.getInstance(), PacketType.Play.Server.PLAYER_INFO) {
				
	            @Override
	            public void onPacketSending(PacketEvent e) {
	                PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) e.getPacket().getHandle();
	                PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) DeathAnimation.getDeclaredField(packet, "a");
	                if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE) {
	                	if(/*deads.contains(KitPlayer.cast(e.getPlayer()))*/ true) {
	                		System.out.print(e.getPlayer().getName()+" gamemode changed to "+e.getPlayer().getGameMode().toString());
		                    @SuppressWarnings("unchecked")
							List<PacketPlayOutPlayerInfo.PlayerInfoData> infoList = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) DeathAnimation.getDeclaredField(packet, "b");
		                    for (PacketPlayOutPlayerInfo.PlayerInfoData infoData : infoList) {
		                        if (infoData.c() == EnumGamemode.SPECTATOR) {
		                            try {
		                                DeathAnimation.modifyFinalField(PacketPlayOutPlayerInfo.PlayerInfoData.class.getDeclaredField("c"), infoData, EnumGamemode.SURVIVAL);
		                            } catch (NoSuchFieldException e1) {
		                                e1.printStackTrace();
		                            }
		                        }
		                    }
	                	}
	                }
	            }
	            
	        });
		}
	}
	
	public static Object getDeclaredField(Object object, String fieldName) {
	    try {
	        Field field = object.getClass().getDeclaredField(fieldName);
	        field.setAccessible(true);
	        return field.get(object);
	    } catch (NoSuchFieldException | IllegalAccessException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	 
	public static void modifyFinalField(Field field, Object target, Object newValue) {
	    try {
	        field.setAccessible(true);
	        Field modifierField = Field.class.getDeclaredField("modifiers");
	        modifierField.setAccessible(true);
	        modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	        field.set(target, newValue);
	    } catch (NoSuchFieldException | IllegalAccessException e) {
	        e.printStackTrace();
	    }
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player && !(e instanceof EntityDamageByEntityEvent)) {
			KitPlayer player = KitPlayer.cast(e.getEntity());
			Double damage = e.getFinalDamage();
			if(player.getHealth()-damage <= 0) {
				playDeathAnimation(player, 5);
				// play player death animation.
				// give player a death
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEnitityDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			KitPlayer player = KitPlayer.cast(e.getEntity());
			Double damage = e.getFinalDamage();
			if(player.getHealth()-damage <= 0) {
				playDeathAnimation(player, 5);
				// play player death animation.
				// give damager a kill
				// give player a death
				e.setCancelled(true);
			}
		}
	}
	
	private void playDeathAnimation(KitPlayer player, int delay) {
		if(!deads.contains(player)) {
			deads.add(player);
			
			GameMode initialGameMode = player.getGameMode();
			player.setGameMode(GameMode.SPECTATOR);
			// send player survival gamemode packet?
			ProtocolManager pm = DependencyManager.getProtocolManager();
			if(pm != null) {
				try {
					PacketContainer packet = pm.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);
					// https://wiki.vg/Protocol#Change_Game_State
					// 3 = reason = gamemodechange
					// 0 = gamemmode = survival
					packet.getIntegers().write(0, 3).write(1, 0);
					pm.sendServerPacket(player.getPlayer(), packet);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			
			// death animation
			new BukkitRunnable() {
				int time = delay + 1;
				@Override
				public void run() {
					time--;
					if(time <= 0) {
						player.setGameMode(initialGameMode);
						cancel();
					} else {
						player.sendTitle("{\"text\":\"YOU DIED!\",\"bold\":true,\"color\":\"red\"}", Placeholder.parse("{\"text\":\"Respawning in {TIME}...\",\"color\":\"gray\"}", player).set("{TIME}", time).getString(), 0, 25, 0);
					}
				}
			}.runTaskTimer(RunicCore.getInstance(), 0, 20);
		}
	}
	
}
