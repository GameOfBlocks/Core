package com.westeroscraft.gob.honor.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.westeroscraft.gob.honor.PlayerRankEntity;

public class RankChangeEvent extends Event {

	private static final long serialVersionUID = 8928056802402693604L;
	private double rank;
	private PlayerRankEntity entity;
	private Player player;
	
	private static final HandlerList handlers = new HandlerList();
	 
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}


	public RankChangeEvent(Player who, double rank,PlayerRankEntity entity) {
		this.player = who;
		this.rank = rank;
		this.entity = entity;
	}

	public double getRank() {
		return rank;
	}
	public PlayerRankEntity getRankEntity() {
		return entity;
	}

	public Player getPlayer() {
		return player;
	}

}
