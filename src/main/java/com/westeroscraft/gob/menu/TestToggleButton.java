package com.westeroscraft.gob.menu;


import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.core.CorePlugin;

public class TestToggleButton implements MenuItem {
	private boolean on = false;
	public String[] getHelpText() {
		// TODO Auto-generated method stub
		String[] help = {"This is a test button to toggle"};
		return help;
	}

	public ItemStack render() {
		ItemStack i = null;
		if(on) {
			i = CraftItemStack.asCraftCopy(new ItemStack(Material.REDSTONE_TORCH_ON));
			CorePlugin.setName(i, "Being Awesome is enabled!");
		} else {
			i = CraftItemStack.asCraftCopy(new ItemStack(Material.TORCH));
			CorePlugin.setName(i, "Being Awesome is disabled!");
		}
		return i;
	}

	public boolean pickup(MenuEvent e) {
		on = !on;
		if(!e.isInInventory()) {
			e.menu.rerenderSlot(e.view.convertSlot(e.rslot));
		}
		return false;
	}

	public boolean place(MenuEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onDelete(Menu m) {
		// TODO Auto-generated method stub
	}



}
