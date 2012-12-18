package com.westeroscraft.gob.shipping;

import java.util.Collection;


public class PendingPackage {
	public static PendingPackage generateUniquePackage(Collection<PendingPackage> list) {
		for(int i = 1; i < Integer.MAX_VALUE; i++) {
			boolean reserved = false;
			for(PendingPackage p: list) {
				if(p.ID == i) {
					reserved = true;
					break;
				}
			}
			if(!reserved){
				PendingPackage pkg = new PendingPackage();
				pkg.ID = i;
				return pkg;
			}
		}
		return null;
	}
	public ShippableInventory source;
	public ShippableInventory destination;
	public int progress;
	public int required;
	public int ID;
}
