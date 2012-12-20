package com.westeroscraft.gob.geo;

import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;



public abstract class Boundable  implements MBR{
	
	//protected double[] minvalues = {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
	//protected double[] maxvalues = {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
	
	protected Point computedCenter = null;
	public boolean intersects(MBR other) {
		// TODO Auto-generated method stub
		return false;
	}
	public <T> boolean intersects(T t, MBRConverter<T> converter) {
		// TODO Auto-generated method stub
		return false;
	}
	public MBR union(MBR mbr) {
		//new SimpleMBR();
		// TODO Auto-generated method stub
		return null;
	}
	
}

