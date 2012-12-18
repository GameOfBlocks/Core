package com.westeroscraft.gob.menu;

import org.bukkit.inventory.Inventory;

public interface PickableMenuItem extends MenuItem {
	public boolean pickup(int slot, Inventory i);
}
