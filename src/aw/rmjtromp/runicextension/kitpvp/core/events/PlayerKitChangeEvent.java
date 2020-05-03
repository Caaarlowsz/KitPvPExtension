package aw.rmjtromp.runicextension.kitpvp.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;
import aw.rmjtromp.runicextension.kitpvp.core.kit.Kit;

public final class PlayerKitChangeEvent extends Event {

	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private KitPlayer player;
	private Kit kit = null;
	
	public PlayerKitChangeEvent(KitPlayer player, Kit kit) {
		this.player = player;
		this.kit = kit;
	}
	
	public KitPlayer getPlayer() {
		return player;
	}
	
	public Kit getKit() {
		return kit;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
	
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
	
}
