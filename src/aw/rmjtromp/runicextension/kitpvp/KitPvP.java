package aw.rmjtromp.runicextension.kitpvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.RunicCore.core.Core;
import aw.rmjtromp.RunicCore.core.other.events.PlayerSpawnEvent;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicPlayer;
import aw.rmjtromp.RunicCore.utilities.PlayerSelector;
import aw.rmjtromp.RunicCore.utilities.RunicCommand;
import aw.rmjtromp.RunicCore.utilities.RunicExtension;
import aw.rmjtromp.RunicCore.utilities.builders.CustomItemBuilder;
import aw.rmjtromp.RunicCore.utilities.configs.Config;
import aw.rmjtromp.RunicCore.utilities.configs.MessageConfig.MESSAGE;
import aw.rmjtromp.RunicCore.utilities.placeholders.Placeholder;
import aw.rmjtromp.runicextension.kitpvp.core.Abilities;
import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;
import aw.rmjtromp.runicextension.kitpvp.core.KitSelector;
import aw.rmjtromp.runicextension.kitpvp.core.Kits;
import aw.rmjtromp.runicextension.kitpvp.core.kit.Kit;

public final class KitPvP extends RunicExtension implements CommandExecutor, TabCompleter {
	
	private static KitPvP kitpvp;
	
	private Config kitsConfig;
	
	private Kits kits = new Kits();
	private KitSelector kitselector;
	
	private RunicItemStack kitselectorItem;
	
	private enum PERMISSION {
		KIT_SELECT_SELF("runic.kitpvp.select"),
		KIT_SELECT_OTHERS("runic.kitpvp.select.others");
		
		private String permission;
		PERMISSION(String permission) {
			this.permission = permission;
		}
		
		@Override
		public String toString() {
			return permission;
		}
	}

	@Override
	public String getName() {
		return "KitPvP";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public void onEnable() {
		kitpvp = this;
		kitsConfig = Config.init("kits").loadFromInputStream(getClass().getResourceAsStream("/kits.yml"));
		Abilities.init();
		
		registerCommand(new RunicCommand("kit")
				.setDescription("RunicCore KitPvP extension")
				.setAliases(Arrays.asList("kitpvp", "kits", "kitselector", "kitgui", "ks"))
				.setUsage("/kit [player|kit]")
				.setExecutor(this)
				.setTabCompleter(this));
	}
	
	public String kitpvp_kit_equipped;
	private String target_not_found, incorrect_usage, kitpvp_kit_equipped_target, kitpvp_kit_already_selected_target, kitpvp_kit_already_selected, no_permission, not_enough_arguments, kitpvp_kit_doesnt_exist, kitpvp_kit_selector_opened;
	@Override
	public void loadConfigurations() {
		kits = kits.loadKits(kitsConfig);
		if(kitselector != null) kitselector.destroy();
		kitselector = KitSelector.init();
		
		if(!Core.getConfig().contains("extensions.kitpvp.kit-selector.item")) {
			List<String> lores = new ArrayList<String>();
			lores.add("&7Right click this item to open");
			lores.add("&7the kit selector menu.");
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.item.item", "chest");
			Core.getConfig().set("extensions.kitpvp.kit-selector.item.name", "&6&lKit Selector &7(Right Click)");
			Core.getConfig().set("extensions.kitpvp.kit-selector.item.lore", lores);
			Core.getConfig().set("extensions.kitpvp.kit-selector.item.tags.action", "openKitSelector");
		}
		kitselectorItem = new CustomItemBuilder(Core.getConfig(), "extensions.kitpvp.kit-selector.item").getItem();
		
		target_not_found = Core.getMessages().getMessage(MESSAGE.TARGET_NOT_FOUND);
		incorrect_usage = Core.getMessages().getMessage(MESSAGE.INCORRECT_USAGE);
		no_permission = Core.getMessages().getMessage(MESSAGE.NO_PERMISSION);
		not_enough_arguments = Core.getMessages().getMessage(MESSAGE.NOT_ENOUGH_ARGUMENTS);

		kitpvp_kit_doesnt_exist = Core.getMessages().getString("extensions.kitpvp.kit-doesnt-exist", "&7Kit &e{KIT} &7does't exist.");
		kitpvp_kit_equipped = Core.getMessages().getString("extensions.kitpvp.kit-equipped", "&8» &e{KIT} &7kit equipped!");
		kitpvp_kit_equipped_target = Core.getMessages().getString("extensions.kitpvp.kit-equipped-target", "&e{KIT} &7kit equipped for &e{TARGET}&7!");
		kitpvp_kit_selector_opened = Core.getMessages().getString("extensions.kitpvp.kitselector-opened-target", "&7Opened kitselector for &e{TARGET}&7.");
		kitpvp_kit_already_selected = Core.getMessages().getString("extensions.kitpvp.kit-already-selected", "&cYou already have a kit equipped.");
		kitpvp_kit_already_selected_target = Core.getMessages().getString("extensions.kitpvp.kit-already-selected-target", "&c{TARGET} already has a kit equipped.");
	}
	
	public static KitPvP getInstance() {
		return kitpvp;
	}
	
	public Kits getKits() {
		return kits;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			if(sender instanceof Player) {
				if(sender.hasPermission(PERMISSION.KIT_SELECT_SELF.toString())) {
					KitPlayer target = KitPlayer.cast(sender);
					if(target.hasKit()) target.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, target).getString());
					else kitselector.openSelector(target);
				} else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
			} else {
				if(sender.hasPermission(PERMISSION.KIT_SELECT_OTHERS.toString())) sender.sendMessage(Placeholder.parse(not_enough_arguments, sender).set("{COMMAND}", label.toLowerCase()+" <player> [kit]").getString());
				else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
			}
		} else if(args.length == 1) {
			// /kit <player|kit>
			if(sender instanceof Player) {
				// /kit <player|kit>
				// either attempting to select kit or open kit menu for a player
				KitPlayer player = KitPlayer.cast(sender);
				if(sender.hasPermission(PERMISSION.KIT_SELECT_SELF.toString())) {
					if(kits.getKit(args[0].toLowerCase()) != null) {
						// player is selecting a kit
						Kit kit = kits.getKit(args[0].toLowerCase());
						kit.equipPlayer(player);
					} else if(args[0].equalsIgnoreCase(sender.getName())) {
						KitPlayer target = KitPlayer.cast(sender); // we already know that the sender is a player
						if(target.hasKit()) target.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, target).getString());
						else kitselector.openSelector(target);
					} else if(sender.hasPermission(PERMISSION.KIT_SELECT_OTHERS.toString())) {
						// player wants to open kit selector for a player
						List<RunicPlayer> targets = PlayerSelector.select(args[0], sender.isOp());
						if(targets.size() > 0) {
							if(sender instanceof Player && targets.size() == 1 && targets.contains(RunicPlayer.cast(sender))) {
								// sender is targeting self
								KitPlayer target = KitPlayer.cast(sender);
								kitselector.openSelector(target);
							} else {
								// sender is targeting others
								// already checked if they have permission to equip other players
								for(RunicPlayer t : targets) {
									KitPlayer target = KitPlayer.cast(t);
									if(!target.hasKit()) {
										kitselector.openSelector(target);
										if(!target.equals(sender)) sender.sendMessage(Placeholder.parse(kitpvp_kit_selector_opened, sender).set("{TARGET}", target.getName()).getString());
									} else if(targets.size() <= 5) {
										if(target.equals(sender)) sender.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, sender).getString());
										else sender.sendMessage(Placeholder.parse(kitpvp_kit_already_selected_target, sender).set("{TARGET}", target.getName()).getString());
									}
								}
							}
						} else sender.sendMessage(Placeholder.parse(target_not_found, sender).set("{TARGET}", args[0]).getString());
					} else sender.sendMessage(Placeholder.parse(kitpvp_kit_doesnt_exist, sender).set("{KIT}", args[0]).getString());
				} else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
			} else {
				// /kit <player>
				if(sender.hasPermission(PERMISSION.KIT_SELECT_SELF.toString())) {
					List<RunicPlayer> targets = PlayerSelector.select(args[0], sender.isOp());
					if(targets.size() > 0) {
						if(kits.getKit(args[1].toLowerCase()) != null) {
							Kit kit = kits.getKit(args[1].toLowerCase());
							if(sender instanceof Player && targets.size() == 1 && targets.contains(RunicPlayer.cast(sender))) {
								// sender is targeting self
								KitPlayer target = KitPlayer.cast(sender);
								if(target.hasKit()) target.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, target).getString());
								else kit.equipPlayer(target);
							} else {
								// sender is targeting others
								// already checked if they have permission to equip other players
								for(RunicPlayer t : targets) {
									KitPlayer target = KitPlayer.cast(t);
									if(!target.hasKit()) {
										kit.equipPlayer(target);
										if(!target.equals(sender)) sender.sendMessage(Placeholder.parse(kitpvp_kit_equipped_target, sender).set("{KIT}", kit.getName()).set("{TARGET}", target.getName()).getString());
									}
								}
							}
						} else sender.sendMessage(Placeholder.parse(kitpvp_kit_doesnt_exist, sender).set("{KIT}", args[1]).getString());
					} else sender.sendMessage(Placeholder.parse(target_not_found, sender).set("{TARGET}", args[0]).getString());
				} else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
			}
		} else if(args.length == 2) {
			// /kit <kit> <player>
			if(sender.hasPermission(PERMISSION.KIT_SELECT_SELF.toString())) {
				List<RunicPlayer> targets = PlayerSelector.select(args[0], sender.isOp());
				if(targets.size() > 0) {
					if(kits.getKit(args[0].toLowerCase()) != null) {
						Kit kit = kits.getKit(args[0].toLowerCase());
						if(sender instanceof Player && targets.size() == 1 && targets.contains(RunicPlayer.cast(sender))) {
							// sender is targeting self
							KitPlayer target = KitPlayer.cast(sender);
							if(target.hasKit()) target.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, target).getString());
							else kit.equipPlayer(target);
						} else {
							// sender is targeting others
							if(sender.hasPermission(PERMISSION.KIT_SELECT_OTHERS.toString())) {
								for(RunicPlayer t : targets) {
									KitPlayer target = KitPlayer.cast(t);
									if(!target.hasKit()) {
										kit.equipPlayer(target);
										if(!target.equals(sender)) sender.sendMessage(Placeholder.parse(kitpvp_kit_equipped_target, sender).set("{KIT}", kit.getName()).set("{TARGET}", target.getName()).getString());
									} else if(targets.size() <= 5) {
										if(target.equals(sender)) sender.sendMessage(Placeholder.parse(kitpvp_kit_already_selected, sender).getString());
										else sender.sendMessage(Placeholder.parse(kitpvp_kit_already_selected_target, sender).set("{TARGET}", target.getName()).getString());
									}
								}
							} else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
						}
					} else sender.sendMessage(Placeholder.parse(kitpvp_kit_doesnt_exist, sender).set("{KIT}", args[1]).getString());
				} else sender.sendMessage(Placeholder.parse(target_not_found, sender).set("{TARGET}", args[0]).getString());
			} else sender.sendMessage(Placeholder.parse(no_permission, sender).getString());
		} else {
			if(sender.isOp()) sender.sendMessage(Placeholder.parse(incorrect_usage, sender).set("{COMMAND}", label.toLowerCase()+" [player]").getString());
			else sender.sendMessage(Placeholder.parse(incorrect_usage, sender).set("{COMMAND}", label.toLowerCase()).getString());
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> suggestion = new ArrayList<String>();
		
		if(args.length == 1 && sender.isOp()) {
			if(args[0].isEmpty()) {
				if(sender instanceof Player) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(((Player) sender).canSee(player)) suggestion.add(player.getName());
					}
				} else {
					for(Player player : Bukkit.getOnlinePlayers()) {
						suggestion.add(player.getName());
					}
				}
			} else {
				if(sender instanceof Player) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(((Player) sender).canSee(player) && player.getName().toLowerCase().startsWith(args[0].toLowerCase())) suggestion.add(player.getName());
					}
				} else {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) suggestion.add(player.getName());
					}
				}
			}
		}
		
		Collections.sort(suggestion);
		return suggestion;
	}
	
	public RunicItemStack getKitSelectorItem() {
		return kitselectorItem;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		KitPlayer player = KitPlayer.cast(e.getEntity());
		
		// TODO check if they have staff mode
		if(player.hasKit()) player.setKit(null);
		if(!player.isVanished()) player.clearInventory();
		player.spigot().respawn();
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());

		if(!player.isVanished()) {
			player.getInventory().setItem(0, getKitSelectorItem());
			player.getInventory().setHeldItemSlot(0);
			player.updateInventory();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		
		// TODO check if they have staff mode
		if(player.hasKit()) player.setKit(null);
		if(!player.isVanished()) {
			player.clearInventory();
			player.getInventory().setItem(0, getKitSelectorItem());
			player.getInventory().setHeldItemSlot(0);
			player.updateInventory();
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(e.getEntity() instanceof Player) {
			KitPlayer player = KitPlayer.cast((Player) e.getEntity());
			if(!player.hasPotionEffect(PotionEffectType.HUNGER)) {
				if(player.getFoodLevel() < 20) player.feed();
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			KitPlayer player = KitPlayer.cast(e.getEntity());
			if(!player.hasKit()) e.setCancelled(true);
			else if(player.isInsideRegion("spawn") || player.isInsideSafeRegion()) e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			KitPlayer player = KitPlayer.cast(e.getEntity());
			KitPlayer damager = KitPlayer.cast(e.getDamager());
			
			if(!player.hasKit() || !damager.hasKit()) e.setCancelled(true);
			else if(player.isInsideRegion("spawn") || damager.isInsideRegion("spawn") || player.isInsideSafeRegion() || damager.isInsideSafeRegion()) e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		
		// TODO check if they have staff mode
		if(player.hasKit()) player.setKit(null);
		if(!player.isVanished()) player.clearInventory();
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		if(player.hasKit()) {
			e.setCancelled(true);
			player.sendMessage("&cBlockPlaceEvent cancelled &7by KitPvP.java:369&c because you have a kit selected.");
		}
	}
	
	@EventHandler
	public void onPlayerSpawn(PlayerSpawnEvent e) {
		KitPlayer player = KitPlayer.cast(e.getPlayer());
		
		if(player.hasKit()) player.setKit(null);
		if(!player.isVanished()) {
			player.clearInventory();
			player.getInventory().setItem(0, getKitSelectorItem());
			player.getInventory().setHeldItemSlot(0);
			player.updateInventory();
			player.removeActivePotionEffects();
		}
	}

}
