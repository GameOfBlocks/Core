package com.westeroscraft.gob.geo;

import org.khelekore.prtree.MBRConverter;

public class BoundableIndexConverter implements MBRConverter<Boundable> {
	public int getDimensions() {
		return 3;
	}

	public double getMax(int dim, Boundable b) {
		return b.getMax(dim);
	}

	public double getMin(int dim, Boundable b) {
		return b.getMin(dim);
	}

}
