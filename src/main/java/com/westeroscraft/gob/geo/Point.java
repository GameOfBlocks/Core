package com.westeroscraft.gob.geo;;

public class Point extends Boundable {
	double[] position = {0.0,0.0,0.0};
	public Point(double x, double y, double z) {
		position[GeometryManager.X] = x;
		position[GeometryManager.Y] = y;
		position[GeometryManager.Z] = z;
		
		this.computedCenter = this;
	}
	public Point(double x, double z) {
		this(x,Double.NaN,z);
	}
	
	public boolean equals(Object other) {
		if(other instanceof Point) {
			Point p = (Point) other;
			return Double.compare(position[0] , p.position[0]) == 0 &&
			Double.compare(position[1] , p.position[1]) == 0 &&
			Double.compare(position[2] , p.position[2]) == 0;
		} else {
			return super.equals(other);
		}
	}
	public int getDimensions() {
		return 3;
	}
	public double getMax(int axis) {
		return this.position[axis];
	}
	public double getMin(int axis) {
		return this.position[axis];
	}
}
