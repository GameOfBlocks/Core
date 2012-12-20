package com.westeroscraft.gob.geo;

public class Magigon extends Boundable {
	
	
	private class edge {
		public Point start = null;
		public Point end = null;
		public edge previous = null;
	}
	
	private boolean empty[] = {true,true,true};
	private edge lastedge = new edge();
	protected boolean path = true;
	private double pathIntensity = 10;
	
	protected double[] minvalues = {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
	protected double[] maxvalues = {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
	
	
	public void setHeight(double min, double max) {
		this.empty[GeometryManager.Y] = false;
		minvalues[GeometryManager.Y] = min;
		maxvalues[GeometryManager.Y] = max;
	}
	
	//Grow the bounding box
	private void growbounds(Point p) {
		for(int dim = 0; dim < minvalues.length; dim++){
			if(Double.isNaN(p.position[dim])) {
				continue;
			} else if(empty[dim]) {
				minvalues[dim] = p.position[dim];
				maxvalues[dim] = p.position[dim];
				empty[dim] = false;
			} else if(minvalues[dim] > p.position[dim]) {
				minvalues[dim] = p.position[dim];
			} else if (maxvalues[dim] < p.position[dim]) {
				maxvalues[dim] = p.position[dim];
			}
		}
	}
	
	public void addPoint(Point p){
		this.growbounds(p);
		if(lastedge.start == null) {
			lastedge.start = p;
		} else if (lastedge.end == null){
			lastedge.end = p;
			edge n =new edge();
			n.previous = lastedge;
			
			//Scan to see if this is the endpoint
			boolean found = false;
			edge cursor = lastedge.previous;
			while(!found) {
				if(cursor != null) {
					if(cursor.start != null) {
						if(cursor.start.equals(p)){
							found = true;
						}
					}
					cursor = cursor.previous;
				} else {
					break;
				}
			}
			if(!found) {
				n.start = p;
			}
			lastedge = n;
		} else {
			edge n =new edge();
			n.previous = lastedge;
			lastedge = n;
			lastedge.start = p;
		}
	}
	
	
	double bmax;
	double bmin;
	private boolean checkbounds(Point p, edge current, int dimension) {
		double max = current.start.position[dimension];
		double min = max;
		if(max < current.end.position[dimension]) {
			max = current.end.position[dimension];
		}
		if(min > current.end.position[dimension]) {
			min = current.end.position[dimension];
		}
		bmax = max;
		bmin = min;
		return (p.position[dimension] <= max && p.position[dimension] >= min);
	}
	
	
	public boolean intersect(Point p) {
		
		edge current = lastedge;
		boolean flip = false;
		
		if(Double.isNaN(p.position[GeometryManager.Y]) || this.minvalues[GeometryManager.Y] < p.position[GeometryManager.Y] && 
				this.maxvalues[GeometryManager.Y] > p.position[GeometryManager.Y]) 
		{
			while(current != null) {
				if(current.start == null || current.end == null) {
					if(current.previous == null) {
						break;
					} else {
						current = current.previous;
						continue;
					}
				}
				
				if(checkbounds(p, current, GeometryManager.Z)) {
					double minz = bmin;
					double maxz = bmax;
					double x = p.position[GeometryManager.X];
					if(!checkbounds(p,current, GeometryManager.X)) {
						if(x >= bmax) {
							flip = !flip;
						}
					} else {
						double zdist = maxz - minz;
						double xdist = bmax - bmin;
						double pdist = maxz - p.position[GeometryManager.Z];
						if(maxz - minz == 0) {
							if(x <= bmax && x >= bmin) {
								flip = !flip;
							}
						} else {
							boolean intersect = ((pdist/zdist)*xdist + bmax <= p.position[GeometryManager.X]);
							flip = (intersect) ? !flip : flip;
						}
					}
				}
				
				current = current.previous;
			}
		}
		
		
		
		return flip;
		
	}

	public int getDimensions() {
		// TODO Auto-generated method stub
		return 3;
	}

	public double getMax(int axis) {
		// TODO Auto-generated method stub
		return this.minvalues[axis];
	}

	public double getMin(int axis) {
		// TODO Auto-generated method stub
		return this.maxvalues[axis];
	}
	

}
