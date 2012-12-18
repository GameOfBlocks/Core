package com.westeroscraft.gob.menu;

import java.util.ArrayList;

import net.minecraft.server.NBTTagCompound;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.menu.MenuItem.behaviour;

public class Menu implements Listener{
	
	private int slot_sorce;
	
	private ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
	
	private Inventory inv; 

	
	public Menu(Inventory i) {
		this.inv = i;
	}
	public static void setMenuIndex(ItemStack item, int id) {
		net.minecraft.server.ItemStack mcItem = ((CraftItemStack) item).getHandle();
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setInt("menu-index", id);
	}
	
	public static int getMenuIndex(ItemStack i) {
		if(i == null) {
			return -1;
		}
		net.minecraft.server.ItemStack mcItem = ((CraftItemStack) i).getHandle();

		if(mcItem.hasTag()) {
			return mcItem.getTag().getInt("menu-index");
		} else {
			return -1;
		}
	}
	
	public void rerender() {
		for(int a = 0; a < this.inv.getSize(); a++) {
			ItemStack i = this.inv.getItem(a);
			if(i != null && i.getType() != Material.AIR) {
				int m = getMenuIndex(i);
				if(m >= 0 && m < menuItems.size()) {
					MenuItem menui = this.menuItems.get(m);
					ItemStack Item = menui.render();
					setMenuIndex(Item, m);
					this.inv.setItem(a, Item);
				}
			}
		}
	}
	
	public void addMenuItem(int slot, MenuItem m) {
		menuItems.add(m);
		int index = menuItems.size() - 1;
		ItemStack Item = m.render();
		setMenuIndex(Item, index);
		this.inv.setItem(slot, Item);
	}
	public boolean addMenuItem(MenuItem m) {
		int slot = this.inv.firstEmpty();
		if(slot >= 0) {
			menuItems.add(m);
			int index = menuItems.size() - 1;
			ItemStack Item = m.render();
			setMenuIndex(Item, index);
			this.inv.setItem(slot, Item);
			return true;			
		} else {
			return false;
		}
		
	}
	@EventHandler (priority = EventPriority.MONITOR)
	public void onInventoryClickMonitor(InventoryClickEvent e) {
		if(e.getView().getTitle() != "Game Menu") {
			return;
		}
		
		if(e.getResult() == Result.ALLOW || e.getResult() == Result.DEFAULT) {
			int rawslot = e.getRawSlot();
			if(rawslot != InventoryView.OUTSIDE) {
				this.slot_sorce = rawslot;
			}
		}
	}
	
	public Inventory getInventory(){
		return this.inv;
	}
	
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		System.out.println("Test registration " + e.getInventory() + "-" + inv);
		if(e.getView().getTitle() != "Game Menu") {
			return;
		}
		
		
		//If the cursor is blank start a pickup tansaction
		if(e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
			if(e.getRawSlot() < 27 && e.getRawSlot() != InventoryView.OUTSIDE) { //Item clicked is inside menu
				//get the menuItem
				int midex = -1;
				ItemStack currenti = e.getCurrentItem();
				if(currenti != null) {
					midex = getMenuIndex(currenti);
				}
				if(midex >= 0 && midex < menuItems.size()) {
					MenuItem i = menuItems.get(midex);
					if(i instanceof PickableMenuItem && !((PickableMenuItem) i).pickup(e.getRawSlot(), e.getInventory())) {
						e.setResult(Result.DENY);
					}
					if(!i.getBehaviours().contains(behaviour.LIFTABLE)) {
						e.setResult(Result.DENY);
					}
					
				}
			}
		}
		
		//If the cursor contains an Item
		if(e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
			int cindex = getMenuIndex(e.getCursor());
			MenuItem i = null;
			if(cindex >= 0 && cindex < menuItems.size()) {
				i = this.menuItems.get(cindex);
			}
			int mindex = getMenuIndex(e.getCursor());
			MenuItem ci = null;
			if(mindex >= 0 && mindex < menuItems.size()) {
				ci = this.menuItems.get(mindex);
			}
			
			
			if(e.getRawSlot() == InventoryView.OUTSIDE) {
				if(i != null && !i.getBehaviours().contains(behaviour.DELETEABLE)) {
					e.setResult(Result.DENY);
				}
			} else if(e.getRawSlot() < 27) { //inside menu
				if(i == null) {
					e.setResult(Result.DENY);
				} else {
					if(i instanceof PlaceableMenuItem && !((PlaceableMenuItem) i).place(ci,e.getRawSlot(), e.getInventory())) {
						e.setResult(Result.DENY);
					}
					if(!i.getBehaviours().contains(behaviour.MOVEABLE)) {
						e.setResult(Result.DENY);
					}
				}
			} else if( e.getRawSlot() >= e.getInventory().getSize()) {
				if(i != null) {
					if(!i.getBehaviours().contains(behaviour.PLAYEROWNABLE)) {
						e.setResult(Result.DENY);
					}
				}
			}
		}
	}
}
