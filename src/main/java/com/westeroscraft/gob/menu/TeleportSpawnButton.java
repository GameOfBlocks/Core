package com.westeroscraft.gob.menu;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.westeroscraft.gob.core.CorePlugin;

public class TeleportSpawnButton implements MenuItem{
	public String[] getHelpText() {
		// TODO Auto-generated method stub
		String[] help = {"This will transport you to spawn"};
		return help;
	}
	
	public ItemStack render() {
		ItemStack i = CraftItemStack.asCraftCopy(new ItemStack(Material.BED));
		CorePlugin.setName(i, "Teleport to spawn!");
		return i;
	}


	public boolean pickup(MenuEvent e) {
		//e.getPlayer().closeInventory();
		e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
		return false;
	}

	public boolean place(MenuEvent e) {
		// Should never be called because players should never be able to pick this item up
		return false;
	}

	public void onDelete(Menu m) {	} //Can never be called because players should not be able to pickup the menu
}
