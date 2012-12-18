package com.westeroscraft.gob.shipping;

import org.bukkit.inventory.Inventory;

public class ShippableInventory {
	public Inventory inventory;
	public String name = "Generic Location";
	public int hashCode() {
		if(inventory != null) {
			return inventory.hashCode();
		} else
		{
			return super.hashCode();
		}
	}
	public boolean equals(Object o) {
		if(inventory != null) {
			return inventory.equals(o);
		} else
		{
			return super.equals(o);
		}
	}
}
