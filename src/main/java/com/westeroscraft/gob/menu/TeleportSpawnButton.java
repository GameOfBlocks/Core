package com.westeroscraft.gob.menu;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.core.CorePlugin;

public class TeleportSpawnButton implements PickableMenuItem{
	private boolean dirty = true;
	private HumanEntity e;
	public Set<behaviour> getBehaviours() {
		return EnumSet.noneOf(behaviour.class);
	}

	public String[] getHelpText() {
		// TODO Auto-generated method stub
		String[] help = {"This will transport you to spawn"};
		return help;
	}

	public boolean isDirty() {
		return dirty;
	}
	
	public TeleportSpawnButton(HumanEntity e) {
		this.e =e;
	}

	public ItemStack render() {
		ItemStack i = new CraftItemStack(Material.BED);
		CorePlugin.setName(i, "Teleport to spawn!");
		return i;
	}

	public boolean pickup(int slot, Inventory i) {
		e.closeInventory();
		e.teleport(e.getWorld().getSpawnLocation());
		return false;
	}



}
