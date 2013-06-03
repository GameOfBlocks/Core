package com.westeroscraft.gob.menu;

import java.util.HashMap;
import java.util.concurrent.Callable;

import net.minecraft.server.v1_4_R1.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.core.CorePlugin;

public class Menu implements Listener{
	
	
	private HashMap<Integer,MenuItem> menuItems = new HashMap<Integer,MenuItem>();
	
	private Inventory inv; 

	
	public Menu(Inventory i) {
		this.inv = i;
		menuItems.put(InventoryView.OUTSIDE, getNullItem());
	}
	
	public static class closefuture implements Callable<Object> {
		
		private HumanEntity e;
		public closefuture(HumanEntity e) {
			this.e =e;
		}
		public Object call() {
			e.closeInventory();
			return null;
		}
	}

	
	private static MenuItem nullitm = new MenuItem() {
		public ItemStack render() {
			return null;
		}

		public String[] getHelpText() {
			return null;
		}

		public boolean pickup(MenuEvent e) {
			if(e.isOutside()) {
				if(e.getCursorItem() != null && e.getCursorItem().getType() != Material.AIR) {
					MenuItem cursor = e.getCursorMenuItem();
					if(cursor != null) {
						cursor.onDelete(e.getMenu());
					}
				} else {
					closefuture fut = new closefuture(e.getPlayer());
					Bukkit.getScheduler().callSyncMethod(Bukkit.getPluginManager().getPlugin("GOB"), fut);
				}
			} else {
				if(e.getCursorItem() != null && e.getCursorMenuItem() == null) {
					
					return false;
				}
			}
			return true;
		}

		public boolean place(MenuEvent e) {
			//This enforces players cannot place non menu items into the menu

			return true;
		}

		public void onDelete(Menu m) {} // You cannot delete the null object
	};
	
	protected MenuItem getNullItem(){
		return Menu.nullitm;
	}
	
	
	public static void setMenuIndex(ItemStack item, int id) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = CorePlugin.getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setInt("menu-index", id);
	}
	public static int getMenuIndex(ItemStack i) {
		if(i == null || i.getType() == Material.AIR) {
			return InventoryView.OUTSIDE;
		}
		net.minecraft.server.v1_4_R1.ItemStack mcItem = CorePlugin.getHandle(i);

		if(mcItem.hasTag()) {
			return mcItem.getTag().getInt("menu-index");
		} else {
			return -1;
		}
	}
	
	public void rerender() {
		for(int a = 0; a < this.inv.getSize(); a++) {
			MenuItem mi = this.getMenuItembySlot(a);
			if(mi != null) {
				ItemStack i = mi.render();
				if(i != null) {
					setMenuIndex(i, getMenuIndex(this.inv.getItem(a))); 
				}
				this.inv.setItem(a, i);
			}
		}
	}
	
	public void rerenderSlot(int slot) {
		MenuItem mi = this.getMenuItembySlot(slot);
		if(mi != null) {
			ItemStack i = mi.render();
			if(i != null) {
				setMenuIndex(i, getMenuIndex(this.inv.getItem(slot))); 
			}
			this.inv.setItem(slot, i);
		}
	}
	
	
	public MenuItem getMenuItembySlot(int slot) {
		return getMenuItembyID(getMenuIndex(this.inv.getItem(slot)));
	}
	public MenuItem getMenuItembyID(int id) {
		return this.menuItems.get(id);
	}
	public void addMenuItem(int slot, MenuItem m) {
		int index = menuItems.size();
		menuItems.put(index,m);
		ItemStack Item = m.render();
		setMenuIndex(Item, index);
		this.inv.setItem(slot, Item);
	}
	public boolean addMenuItem(MenuItem m) {
		int slot = this.inv.firstEmpty();
		if(slot >= 0) {
			this.addMenuItem(slot, m);
			return true;
		} else {
			return false;
		}
	}
	public Inventory getInventory(){
		return this.inv;
	}
	
	
	
	//Note: The order of processing is this Cursor then the slot
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		System.out.println("Test registration " + e.getInventory() + "-" + inv);
		if(e.getView().getTitle() != "Game Menu") {
			return;
		}
		
		
		//If the cursor is blank start a pickup tansaction
		
		MenuEvent Mevent = new MenuEvent(e.getView(),this,e.getRawSlot());
		MenuItem currentMenuItem = Mevent.getCursorMenuItem();
		boolean placeresult = true;
		boolean pickupresult = true;
		
		if(currentMenuItem != null) {
			placeresult = currentMenuItem.place(Mevent);
		}
		currentMenuItem = Mevent.getClickedMenuItem();
		
		if(currentMenuItem != null) {
			pickupresult = currentMenuItem.pickup(Mevent);
		}
		
		if(!placeresult || !pickupresult) {
			e.setResult(Result.DENY);
		}
	}
}
