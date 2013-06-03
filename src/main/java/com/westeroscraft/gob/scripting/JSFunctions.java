package com.westeroscraft.gob.scripting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.westeroscraft.gob.core.CorePlugin;


public class JSFunctions {
	HashSet<Player> locked = new HashSet<Player>();
	
	private CorePlugin plugin;
	public JSFunctions(CorePlugin p) {
		super();
		plugin = p;
	}
	public static Object scopeflag = new Object();
	private class waitResumeEvent implements Runnable {
		ContinuationPending pctx;
		Context ctx;
		public void run() {
			
			try {
			ctx.resumeContinuation(pctx.getContinuation(), (Scriptable) pctx.getApplicationState(), null);
			} catch (ContinuationPending p) {
				if(p.getApplicationState() == scopeflag) {
					p.setApplicationState(pctx.getApplicationState());
				}
//				ctx.resumeContinuation(p.getContinuation(), (Scriptable) p.getApplicationState(), p.getApplicationState());
			}
		}
	};
	
	public NPC spawn(ScriptableObject n, String name, String world, double x, double y, double z, float pitch, float yaw) {
		Object obj = ScriptableObject.getProperty(n, "type");
		EntityType type = EntityType.PLAYER;
		if(obj != null && obj instanceof String){
			type = EntityType.fromName((String)obj);
		}
		NPC cNpc = CitizensAPI.getNPCRegistry().createNPC(type, name);
		plugin.registry.put(cNpc, n);
		Location l = new Location(plugin.getServer().getWorld(world),x,y,z);
		l.setPitch(pitch);
		l.setYaw(yaw);
		cNpc.spawn(l);
		return cNpc;
	}
	public void updateLog(Player p){
		this.plugin.updateQuestLog(p);
	}
	public void waitTime(int time) {
		Context ctx = Context.getCurrentContext();
		ContinuationPending pctx = ctx.captureContinuation();
		waitResumeEvent re = new waitResumeEvent();
		pctx.setApplicationState(scopeflag);
		re.pctx = pctx;
		
		re.ctx = ctx;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, re, time);
		throw pctx;
	}
	public boolean lockPlayer(Player p) {
		if(locked.contains(p)) {
			return false;
		} else {
			locked.add(p);
			return true;
		}
	}
	public void unlockPlayer(Player p) {
		locked.remove(p);
	}
	public int questItemCount(Player p, String s) {
		 PlayerInventory inv = p.getInventory();
		 ListIterator<ItemStack> itr = inv.iterator();
		 int count = 0;
		 while(itr.hasNext()) {
			 ItemStack is = itr.next();
			 if(is != null && plugin.getQuestTag(is).equals(s)) {
				 System.out.println(plugin.getQuestTag(is));
				 count++;
			 }
		 }
		return count;
	}
	public Scriptable getQuestItem(ItemStack is) {
		Scriptable obj = new NativeObject();
		if(is == null) {
			ScriptableObject.putProperty(obj, "id", new Integer(0));
			return obj;
		}
		ScriptableObject.putProperty(obj, "id", new Integer(is.getTypeId()));
		ScriptableObject.putProperty(obj, "amount", new Integer(is.getAmount()));
		ScriptableObject.putProperty(obj, "data", new Short((short)is.getData().getData()));
		ScriptableObject.putProperty(obj, "name", plugin.getName(is));
		ScriptableObject.putProperty(obj, "bound", plugin.getBoundPlayer(is));
		ScriptableObject.putProperty(obj, "qid", plugin.getQuestTag(is));
		return obj;
	}
	public boolean matchQID(ItemStack is, String s) {
		String qid = plugin.getQuestTag(is);
		if(qid.equals(s)) {
			return true;
		} else {
			return false;
		}
	}
	public void debug(String s) {
		plugin.getLogger().log(Level.INFO, s);
	}
	public int giveQuestItem(Player p, ScriptableObject item,int amount) {
		Object o = ScriptableObject.getProperty(item, "id");
		int totaladded = 0;
		if(o != Scriptable.NOT_FOUND && o instanceof Number) {
			Number num = (Number) o;
			int id = num.intValue();
			short data = 0;
			o = ScriptableObject.getProperty(item, "data");
			if(o != Scriptable.NOT_FOUND && o instanceof Number) {
				data = ((Number) o).shortValue();
			}
			
			ItemStack is = CraftItemStack.asCraftCopy(new ItemStack(id,amount,data));
			
			
			o = ScriptableObject.getProperty(item, "name");
			if(o != Scriptable.NOT_FOUND && o instanceof String) {
				plugin.setName(is, (String)o);
			}
			
			o = ScriptableObject.getProperty(item, "qid");
			if(o != Scriptable.NOT_FOUND && o instanceof String) {
				plugin.setQuestTag(is, (String) o);
			}
			
			o = ScriptableObject.getProperty(item, "binds");
			if(o == Scriptable.NOT_FOUND || (o instanceof Boolean && !((Boolean)o).booleanValue())) {
				plugin.setPlayerBound(is, p.getName());
			}
			
			
			plugin.describe(is);
			HashMap<Integer, ItemStack> result = p.getInventory().addItem(is);
			totaladded = amount;
			if(!result.isEmpty()) {
				int a = result.get(0).getAmount();
				totaladded -= a;
			}
			
		}
		return totaladded;
	}
	
	public Scriptable getPersistance(String s) {
		Scriptable pData = plugin.loadPlayerData(s);
		return pData;
	}
	public Scriptable getPersistance(Player p) {
		return this.getPersistance(p.getName());
	}
	
	//Use 0 for all occurances
	public int removeQuestItems(Player p, String s, int count) { 
		 PlayerInventory inv = p.getInventory();
		 ListIterator<ItemStack> itr = inv.iterator();
		 boolean removeAll =  false;
		 int start = count;
		 if ( count == 0) {
			 removeAll = true;
		 }
		 while(itr.hasNext()) {
			 ItemStack is = itr.next();
			 if(is != null && plugin.getQuestTag(is).equals(s)) {
				 
				 int available = is.getAmount();
				 available = available - count;
				 if(available <= 0 || removeAll) {
					 count -= is.getAmount();
					 is.setType(Material.AIR);
				 } else {
					 count -= is.getAmount() - available;
					 is.setAmount(available);
				 }
			 }
		 }
		 return count-start;
	}

}
