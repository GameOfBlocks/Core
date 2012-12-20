package com.westeroscraft.gob.geo;

import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.NodeFilter;
import org.khelekore.prtree.PointND;

public class FeatureFilter implements NodeFilter<Boundable>, DistanceCalculator<Boundable>{
	int level;
	public FeatureFilter(int minlevel) {
		this.level = minlevel;
	}
	public boolean accept(Boundable b) {
		if(b instanceof Feature) {
			if(((Feature)b).level >= level) {
				return true;
			}
		}
		return false;
	}
	public double distanceTo(Boundable geom, PointND p) {
		if(geom.computedCenter == null) {
			
			double x = geom.getMax(GeometryManager.X);
			double y = geom.getMax(GeometryManager.Y);
			double z = geom.getMax(GeometryManager.Z);
			
			x += geom.getMin(GeometryManager.X);
			y += geom.getMin(GeometryManager.Y);
			z += geom.getMin(GeometryManager.Z);
			
			x /= 2.0;
			y /= 2.0;
			z /= 2.0;
			geom.computedCenter = new Point(x,y,z);
		}
		double component = 0;
		for(int dim = 0; dim < p.getDimensions(); dim++) {
			double dimension = p.getOrd(dim)-geom.computedCenter.position[dim];
			if(Double.isNaN(dimension) || Double.isInfinite(dimension)) {
				continue;
			}
			dimension = Math.pow(dimension, 2);
			component += dimension;
		}		
		return Math.sqrt(component);
	}

}
