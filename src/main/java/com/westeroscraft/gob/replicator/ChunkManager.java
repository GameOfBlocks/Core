package com.westeroscraft.gob.replicator;

public class ChunkManager {
	int port;
	String host;
	ChunkManager() {
		host = "127.0.0.1";
		port = 26656;		
	}
	ChunkManager(String hostname, int port) {
		host = hostname;
		this.port = port;
	}
	
}
