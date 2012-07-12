package com.westeroscraft.gob.honor;

public abstract class RankEntity {
	protected double rank;
	private long lastupdate; 
	public static double kFactor = 32;
	protected RankEntity()	{
		lastupdate = System.currentTimeMillis();
		rank = 0;
	}
	protected double calculate(double comparedrate, double result) {
		double mer = getRank();
		double delta = comparedrate - mer;
		double chance = 1.0/(1.0 +Math.pow(10, delta/400.0));
		System.out.println(chance);
		double ratedelta = RankEntity.kFactor*(result - chance);
		if(ratedelta > kFactor)
			ratedelta = kFactor;
		if(ratedelta < -kFactor)
			ratedelta = -kFactor;
		return mer + ratedelta;
	}

	public void setRank(double newrank) {
		rank = newrank;		
	}
	public double getRank() {
		return rank;
	}
	protected abstract void save();
	public long getLastUpdate() {
		return lastupdate;
	}
	protected void update() {
		lastupdate = System.currentTimeMillis();
	}
}
