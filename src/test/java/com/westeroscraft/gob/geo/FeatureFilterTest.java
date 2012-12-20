package com.westeroscraft.gob.geo;

import org.junit.Test;
import org.khelekore.prtree.SimplePointND;


public class FeatureFilterTest {
	
	@Test
	public void testOk() {
		double ok = Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY;
		
		FeatureFilter ff = new FeatureFilter(0);
		double distance = ff.distanceTo(new Point(2,4), new SimplePointND(2,5));
		
		
		System.out.println(distance);
		
		
		
		Magigon mg = new Magigon();
		
		//Super simple Square
		mg.addPoint(new Point(0,0));
		mg.addPoint(new Point(0,1));
		mg.addPoint(new Point(1,1));
		mg.addPoint(new Point(1,0));
		mg.addPoint(new Point(0,0));
		boolean test = mg.intersect(new Point(0.5,1));
		
		System.out.println(test);
		
	}

}
