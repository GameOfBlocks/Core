package com.westeroscraft.gob.honor;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.westeroscraft.gob.honor.events.RankChangeEvent;

public class RankManager {
	//private HashSet<RankEntity> loadedranks;
	private HashMap<String,PlayerRankEntity> loadedranks;
	private Queue<RankEntity> saveQ;
	private JavaPlugin plugin;
	private AsyncSaver saver;
	private ConfigurationSection config;
	private File playerdata;
	
	public RankManager(JavaPlugin plugin, ConfigurationSection config) {
		this.plugin = plugin;
		this.config = config;
		saver = new AsyncSaver();
		saveQ = new LinkedList<RankEntity>();
		loadedranks = new HashMap<String,PlayerRankEntity>();
		playerdata = new File(this.plugin.getDataFolder(),"rankings");
		playerdata.mkdirs();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EntityCleaner(), 20*(60*2));
	}
	
	//Gets the current rank of the specified entity
	public double getRank(RankEntity entity) {
		return 0;
	}
	
	//Sets the absolute rank of the entity
	public void setRank(RankEntity entity, double newrank) {
		entity.setRank(newrank);
		return;
	}
	
	public void broadcastUpdate(PlayerRankEntity playerentity) {
		Player p = plugin.getServer().getPlayer(playerentity.getPlayer());
		if(p != null) {
			p.sendMessage("ELO rank: " + Math.round(playerentity.getRank()));
		}
	}
	public void broadcastUpdate(PlayerRankEntity playerentity, double oldrank) {
		Player p = plugin.getServer().getPlayer(playerentity.getPlayer());
		if(p != null) {
			//sendMessage("ELO rank: " + Math.round(playerentity.getRank()));
			p.sendMessage("New rank: " + Math.round(playerentity.getRank()) + ", thats a change of: " + Math.round(playerentity.getRank() - oldrank));
			//System.out.println("Was: " + Math.round(oldrank) + ", thats a change of: " + Math.round(playerentity.getRank() - oldrank));
			
			
			//TODO
			p.setTotalExperience(0);
			p.setLevel((int) Math.round(playerentity.getRank()));
			
			
			RankChangeEvent rankEvent = new RankChangeEvent(p,playerentity.getRank(),playerentity);
			plugin.getServer().getPluginManager().callEvent(rankEvent);
		}
	}
	
	//Get the rank entity for the specified player
	public PlayerRankEntity getPlayerRankEntity(Player player) {
		return getPlayerRankEntity(player.getName());
	}
	
	//Get the rank entity for the specified player
	public PlayerRankEntity getPlayerRankEntity(String name) {
		if(this.loadedranks.containsKey(name)) {
			return this.loadedranks.get(name);
		}
		
		PlayerRankEntity entity = new PlayerRankEntity(name, this.playerdata);
		this.loadedranks.put(name, entity);
		return entity;
	}
	
	protected void updateRank(RankEntity ra, double compare, double result) {
		setRank(ra,ra.calculate(compare, result));
		return;
	}
	
	public void scheduleSave(RankEntity entity) {
		boolean empty = saveQ.isEmpty();
		saveQ.offer(entity);
		if(empty) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, saver, 20);
		}
	}
	public void saveAll(boolean async) {
		Iterator<PlayerRankEntity> entities = loadedranks.values().iterator();
		while(entities.hasNext()) {
			RankEntity entity = entities.next();
			if(async) {
				scheduleSave(entity);
			} else {
				entity.save();
			}
		}
	}
	public boolean isOld(RankEntity entity) {
		return false;
	}
	private class EntityCleaner implements Runnable {
		public void run() {
			Iterator<PlayerRankEntity> entities = loadedranks.values().iterator();
			while(entities.hasNext()) {
				RankEntity entity = entities.next();
				if(isOld(entity)) {
					scheduleSave(entity);
				}
			}
		}
	}
	protected void checkPlayerTimeout(PlayerRankEntity playerentity)
	{
		
		boolean changed = false;
		long timespent = System.currentTimeMillis() - playerentity.getPlayerLastUpdate();
		long timethreshold = config.getLong("playerkilltimeout",1000*60*60*10);
		//long penaltyfactor = config.getLong("playerpenaltyfactor",1000*60*60*24);
		if(timespent >= timethreshold)
		{
			double deltafactor = (timespent-timethreshold)/timethreshold;
			double maxrank = (playerentity.getEnvironmentRank().getRank() + 1500) - (deltafactor*50);
			if(playerentity.getRank() > maxrank) {
				playerentity.setRank(maxrank);
				changed = true;
			}
			
		}
		if(changed) {
			Player p = plugin.getServer().getPlayer(playerentity.getPlayer());
			if(p != null) {
				p.sendMessage("Due to pvp inactivity your rank has been adjusted and is now: " + Math.round(playerentity.getRank()));
			}
		}
	}
	private class AsyncSaver implements Runnable {
		public void run() {
			RankEntity entity = saveQ.poll();
			if(entity != null && loadedranks.containsValue(entity)) {
				entity.save();
				if(isOld(entity)) {
					loadedranks.remove(entity);
				}
			}
			if(!saveQ.isEmpty()) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 20);
			}
		}
	}
	public float getHintBar(Player hitter, Player victim) {

		float bar = 0.5f;
		
		//The environment/monster killed the player
		//This should never happen
		/*
		if(hitter == null && victim != null){
			PlayerRankEntity entity = this.getPlayerRankEntity(victim);
			EnvironmentRankEntity env = entity.getEnvironmentRank();
			
		}*/
		
		//The player killed a monster/mob
		if(hitter != null && victim == null){
			PlayerRankEntity entity = this.getPlayerRankEntity(hitter);
			EnvironmentRankEntity env = entity.getEnvironmentRank();
			
			bar = (float) ((entity.calculate(env.getRank(), 1) - entity.getRank())  / RankEntity.kFactor) ;
		}
		
		//The player killed another player
		if(hitter != null && victim != null){
			PlayerRankEntity entity = this.getPlayerRankEntity(hitter);
			PlayerRankEntity ventity = this.getPlayerRankEntity(victim);
			
			bar = (float) ((entity.calculate(ventity.getRank(), 1) - entity.getRank())  / RankEntity.kFactor) ;
		}
		return bar;
	}
	public void performupdate(Player hitter, Player victim) {
		
		//The environment/monster killed the player
		if(hitter == null && victim != null){
			PlayerRankEntity entity = this.getPlayerRankEntity(victim);
			EnvironmentRankEntity env = entity.getEnvironmentRank();
			double envold = env.getRank();
			double eold = entity.getRank();
			this.updateRank(env, eold, 1);
			this.updateRank(entity, envold, 0);
			this.broadcastUpdate(entity, eold);
		}
		
		//The player killed a monster/mob
		if(hitter != null && victim == null){
			PlayerRankEntity entity = this.getPlayerRankEntity(hitter);
			EnvironmentRankEntity env = entity.getEnvironmentRank();
			double envold = env.getRank();
			double eold = entity.getRank();
			this.updateRank(entity, envold, 1);
			this.updateRank(env, eold, 0);
			this.broadcastUpdate(entity, eold);
		}
		
		//The player killed another player
		if(hitter != null && victim != null){
			PlayerRankEntity entity = this.getPlayerRankEntity(hitter);
			PlayerRankEntity ventity = this.getPlayerRankEntity(victim);
			double vold = ventity.getRank();
			double eold = entity.getRank();
			this.updateRank(entity, vold , 1);
			this.updateRank(ventity, eold, 0);
			this.broadcastUpdate(entity, eold);
			this.broadcastUpdate(ventity, vold);
		}
		
		// TODO Auto-generated method stub
		
	}
}
