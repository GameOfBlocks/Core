package com.westeroscraft.gob.faction;

import java.lang.reflect.Method;

public interface CommandHandler {
	public class MethodPair {
		public Method m;
		public Object o;
		MethodPair() {}
		MethodPair(Method m, Object o) {
			this.m = m;
			this.o = o;
		}
	}

}
