package com.westeroscraft.gob.menu;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

public interface MenuItem {
	enum behaviour {
		MOVEABLE, DELETEABLE, PLAYEROWNABLE, LIFTABLE
	};
	public boolean isDirty();
	public ItemStack render();
	public Set<behaviour> getBehaviours();
	public String[] getHelpText();
}
