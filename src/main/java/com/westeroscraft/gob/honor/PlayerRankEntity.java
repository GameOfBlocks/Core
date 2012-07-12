package com.westeroscraft.gob.honor;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerRankEntity extends RankEntity {
	private EnvironmentRankEntity env;
	private String playername;
	private File data;
	private long playerupdate;
	protected PlayerRankEntity(String name, File dataFolder) {
		data = new File(dataFolder,name + ".yml");
		playername = name;
		load();
	}
	private void load() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(data);
		} catch (Exception e) {	}
		this.setRank(config.getDouble("rank", 100));
		env = new EnvironmentRankEntity();
		env.setRank(config.getDouble("env-rank", 1000));
		playerupdate = config.getLong("player-last-update", System.currentTimeMillis());
	}
	public String getPlayer() {
		return playername;
	}
	public EnvironmentRankEntity getEnvironmentRank() {
		return env;
	}
	protected void save() {
		YamlConfiguration config = new YamlConfiguration();
		config.set("rank", getRank());
		config.set("last-update", getLastUpdate());
		config.set("player-last-update", getPlayerLastUpdate());
		config.set("env-rank", env.getRank());
		try {
			config.save(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public long getPlayerLastUpdate() {
		// TODO Auto-generated method stub
		return playerupdate;
	}
	private void setPlayerLastUpdate(long time)	{
		playerupdate = time;
	}
	public void updatePlayer() {
		this.setPlayerLastUpdate(System.currentTimeMillis());
	}
}
