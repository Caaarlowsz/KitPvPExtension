package aw.rmjtromp.runicextension.kitpvp.core;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;

import aw.rmjtromp.RunicCore.RunicCore;
import aw.rmjtromp.RunicCore.core.Core;
import aw.rmjtromp.RunicCore.core.other.events.RunicCoreReloadEvent;
import aw.rmjtromp.RunicCore.core.other.extensions.RunicItemStack;
import aw.rmjtromp.RunicCore.utilities.essential.RunicGUI;
import aw.rmjtromp.runicextension.kitpvp.KitPvP;
import aw.rmjtromp.runicextension.kitpvp.core.kit.Kit;

public final class KitSelector extends RunicGUI implements Listener {

	private KitSelector() {
		super(Core.getConfig(), "extensions.kitpvp.kit-selector.gui");
		Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
		for(Kit kit : KitPvP.getInstance().getKits().getKits()) {
			addItem(kit.getName(), kit.getDisplayItem().getDisplayItem(), kit.getDisplayItem().getSlot());
		}
	}
	
	public static KitSelector init() {
		restoreDefaults();
		return new KitSelector();
	}
	
	public void openSelector(KitPlayer player) {
		player.openInventory(getInventory(player));
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent e) {
		if(e.getSource().getTitle().equals(this.getTitle())) e.setCancelled(true);
		else {
			RunicItemStack item = new RunicItemStack(e.getItem());
			if(item.hasNBTTag("action") && item.getNBTTagAsString("action").equals("openKitSelector")) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().getName().equals(this.getTitle())) {
			e.setCancelled(true);

			if(e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
				KitPlayer player = KitPlayer.cast((Player) e.getWhoClicked());
				RunicItemStack RIC = new RunicItemStack(e.getCurrentItem());
				if(RIC.hasNBTTag("selectKit")) {
					Kit kit = KitPvP.getInstance().getKits().getKit(RIC.getNBTTagAsString("selectKit"));
					if(kit != null) {
						player.getOpenInventory().close();
						kit.equipPlayer(player);
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent e) {
		if(e.getInventory().getName().equals(this.getTitle())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onRunicCoreReload(RunicCoreReloadEvent e) {
		if(!e.isCancelled()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(player.getOpenInventory() != null) {
					if(getTitle().equals(player.getOpenInventory().getTitle())) {
						player.closeInventory();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent e) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getOpenInventory() != null) {
				if(getTitle().equals(player.getOpenInventory().getTitle())) {
					player.closeInventory();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			KitPlayer player = KitPlayer.cast(e.getPlayer());
			if(!player.hasKit()) {
				if(player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
					RunicItemStack item = new RunicItemStack(player.getItemInHand());
					if(item.hasNBTTag("action") && item.getNBTTagAsString("action").equals("openKitSelector")) {
						openSelector(player);
						player.sendMessage("&cPlayerInteractEvent cancelled &7by KitSelector.java:116&c because item has 'action=openKitSelector' tag.");
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		RunicItemStack item = new RunicItemStack(e.getItemDrop().getItemStack());
		if(item.hasNBTTag("action") && item.getNBTTagAsString("action").equals("openKitSelector")) {
			e.setCancelled(true);
		}
	}
	
	public KitSelector destroy() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getOpenInventory() != null) {
				if(getTitle().equals(player.getOpenInventory().getTitle())) {
					player.closeInventory();
				}
			}
		}
		HandlerList.unregisterAll(this);
		return null;
	}
	
	private static void restoreDefaults() {
		if(!Core.getConfig().contains("extensions.kitpvp.kit-selector.gui.title")) Core.getConfig().getString("extensions.kitpvp.kit-selector.gui.title", "&8Kit Selector");
		if(!Core.getConfig().contains("extensions.kitpvp.kit-selector.gui.size")) Core.getConfig().getInt("extensions.kitpvp.kit-selector.gui.size", 54);
		if(!Core.getConfig().contains("extensions.kitpvp.kit-selector.gui.contents")) {
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier0.name", "&5&lLevel 0 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier0.item", "purplestainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier0.slot", 0);
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier1.name", "&9&lLevel 1 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier1.item", "bluestainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier1.slot", 9);
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier2.name", "&a&lLevel 2 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier2.item", "limestainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier2.slot", 18);
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier3.name", "&e&lLevel 3 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier3.item", "yellowstainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier3.slot", 27);
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier4.name", "&6&lLevel 4 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier4.item", "orangestainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier4.slot", 36);
			
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier5.name", "&c&lLevel 5 Kits");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier5.item", "redstainedglasspane");
			Core.getConfig().set("extensions.kitpvp.kit-selector.gui.contents.tier5.slot", 45);
		}
	}

}
