package com.westeroscraft.gob.menu;

import org.bukkit.inventory.ItemStack;


public interface MenuItem {
	public ItemStack render();
	public String[] getHelpText();
	public boolean pickup(MenuEvent e);
	public boolean place(MenuEvent e);
	public void onDelete(Menu m);
}
