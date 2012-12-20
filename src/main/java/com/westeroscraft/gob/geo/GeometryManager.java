package com.westeroscraft.gob.geo;

import java.util.HashSet;

import org.khelekore.prtree.PRTree;

public class GeometryManager {
	
	private PRTree<Boundable> prtree;
	
	public static final int X = 0;
	public static final int Y = 2;
	public static final int Z = 1;
	
	
	HashSet<Boundable> indexed =  new HashSet<Boundable>();

	public GeometryManager() {
		this.prtree = new PRTree<Boundable>(new BoundableIndexConverter(), 5);
	}
	
	
	public void LoadIndex() {
		
		indexed.toArray(new Boundable[0]);
	}
	
	
}
