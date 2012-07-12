package com.westeroscraft.gob.core.properties;

import com.westeroscraft.gob.core.Property;

public class HonorProperty implements Property {
	private int honor;
	
	HonorProperty() {
		honor = 0;
	}
	
	HonorProperty(int honor) {
		this.honor = honor;
	}

	public void setHonor(int honor) {
		this.honor = honor;
	}

	public int getHonor() {
		return honor;
	}

}
