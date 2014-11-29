package com.basdv98.plugins.lightweightessentials;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KickEvent extends Event {

	Player p;
	KickType t;
	
	public KickEvent(Player p, KickType t) {
		this.p = p;
		this.t = t;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public KickType getType() {
		return t;
	}
	
	private static final HandlerList handlers = new HandlerList();
	 
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}