package com.westeroscraft.gob.menu;

import org.bukkit.inventory.Inventory;

public interface PlaceableMenuItem extends MenuItem, PickableMenuItem {
	public boolean place(MenuItem menuitem, int slot, Inventory I);

}
