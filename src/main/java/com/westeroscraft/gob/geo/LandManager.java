package com.westeroscraft.gob.geo;

import org.khelekore.prtree.MBRConverter;

import com.vividsolutions.jts.geom.Geometry;

public class LandManager {
	
	private class jtsgeomconverter implements MBRConverter<Geometry>{

		public int getDimensions() {
			return 2;
		}

		public double getMax(int d, Geometry g) {
			switch(d) {
			case 0:
				return g.getEnvelopeInternal().getMaxX();
			case 1:
				return g.getEnvelopeInternal().getMaxY();
			}
			return 0;
		}

		public double getMin(int d, Geometry g) {
			switch(d) {
			case 0:
				return g.getEnvelopeInternal().getMinX();
			case 1:
				return g.getEnvelopeInternal().getMinY();
			}
			return 0;
		}
	}
	
	public LandManager() {
		
	}

}
