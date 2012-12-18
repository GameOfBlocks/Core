package com.westeroscraft.gob.menu;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.core.CorePlugin;

public class TestToggleButton implements PickableMenuItem{
	private boolean on = false;
	private boolean dirty = true;
	public Set<behaviour> getBehaviours() {
		return EnumSet.noneOf(behaviour.class);
	}

	public String[] getHelpText() {
		// TODO Auto-generated method stub
		String[] help = {"This is a test button to toggle"};
		return help;
	}

	public boolean isDirty() {
		return dirty;
	}

	public ItemStack render() {
		ItemStack i = null;
		if(on) {
			i = new CraftItemStack(Material.REDSTONE_TORCH_ON);
			CorePlugin.setName(i, "Being Awesome is enabled!");
		} else {
			i = new CraftItemStack(Material.TORCH);
			CorePlugin.setName(i, "Being Awesome is disabled!");
		}
		return i;
	}

	public boolean pickup(int slot, Inventory i) {
		on = !on;
		i.setItem(slot, render());
		return false;
	}



}
