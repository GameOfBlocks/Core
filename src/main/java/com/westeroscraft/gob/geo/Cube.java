package com.westeroscraft.gob.geo;

public class Cube extends Boundable {
	protected double[] minvalues = {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
	protected double[] maxvalues = {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
	
	
	public Cube() {
		
	}
	public Cube(double x1, double z1, double x2, double z2) {
		
	}
	public Cube(double x1, double y1, double z1, double x2, double y2, double z2) {
		
	}
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 3;
	}

	public double getMax(int axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getMin(int axis) {
		// TODO Auto-generated method stub
		return 0;
	}

}
