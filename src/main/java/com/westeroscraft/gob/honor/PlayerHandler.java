package com.westeroscraft.gob.honor;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerHandler implements Listener {
	private RankManager manager;

	public PlayerHandler(RankManager rm){
		manager = rm;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		manager.scheduleSave(manager.getPlayerRankEntity(p));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		manager.checkPlayerTimeout(manager.getPlayerRankEntity(p));
		manager.refreshHonorBar(p);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getRightClicked() instanceof Player) {
			event.getPlayer().setExp(manager.getHintBar(event.getPlayer(), (Player)event.getRightClicked()));
		} else if(event.getRightClicked() instanceof Monster) {
			event.getPlayer().setExp(manager.getHintBar(event.getPlayer(), null));
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(EntityDeathEvent event) {
		event.setDroppedExp(0);
		if(event instanceof PlayerDeathEvent) {
			PlayerDeathEvent pevent = (PlayerDeathEvent)event;
			manager.refreshHonorBar(pevent.getEntity());
			pevent.setKeepLevel(true);
		}
	}
	@EventHandler
	public void onEnchantList(PrepareItemEnchantEvent event) {
		int[] lvls = event.getExpLevelCostsOffered();
		for(int i = 0; i < lvls.length; i++) {
			lvls[i] = 0;
		}
	}
}
