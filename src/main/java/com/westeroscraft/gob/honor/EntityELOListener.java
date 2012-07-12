package com.westeroscraft.gob.honor;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


public class EntityELOListener  implements Runnable, Listener{
	private RankManager rankman; 
	private HashMap<Entity,LastHit> hitlist;
	public class LastHit {
		Player hitter;
		long time;
		public LastHit(Player p) {
			hitter = p;
			time = System.currentTimeMillis();
		}
	}
	public EntityELOListener(RankManager man) {
		rankman = man;
		hitlist = new HashMap<Entity,LastHit>();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		if(!(event.getEntity() instanceof Player || event.getEntity() instanceof Monster))
			return;
		System.out.println("debug line 34");
		LastHit lh = hitlist.get(event.getEntity());
		if(lh != null && System.currentTimeMillis() - lh.time < 1000*60) {
			if(event.getEntity() instanceof Player) {
				rankman.performupdate(lh.hitter,(Player)event.getEntity());
			} else {
				rankman.performupdate(lh.hitter,null);
			}
			hitlist.remove(lh);
		} else {
			if(event.getEntity() instanceof Player) {
				rankman.performupdate(null,(Player)event.getEntity());
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent devent = (EntityDamageByEntityEvent) event;
			Entity hitter = devent.getDamager();
			hitter = (hitter instanceof Projectile) ? ((Projectile) hitter).getShooter() : hitter;
			if(hitter instanceof Player) {
				Player ph = ((Player)hitter);
				LastHit lh = new LastHit(ph);
				hitlist.put(event.getEntity(),lh);
				if(event.getEntity() instanceof Player) {
					Player pv = (Player)event.getEntity();
					ph.setExp(rankman.getHintBar((Player)hitter, (Player)event.getEntity()));
					pv.setExp(rankman.getHintBar(pv, ph));
				} else {
					ph.setExp(rankman.getHintBar((Player)hitter, null));
				}
			} else {
				if(event.getEntity() instanceof Player) {
					Player p = (Player)event.getEntity();
					p.setExp(rankman.getHintBar(p,null));
				}
			}
		}
	}
	public void run() {
		Iterator<Entity> itr = hitlist.keySet().iterator();
		while(itr.hasNext()) {
			Entity e = itr.next();
			LastHit lh = hitlist.get(e);
			if(System.currentTimeMillis() - lh.time > 1000*60) {
				itr.remove();
			}
		}
	}
}
