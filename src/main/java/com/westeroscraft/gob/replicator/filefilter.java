package com.westeroscraft.gob.replicator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;

public class filefilter extends JavaPlugin {
	
	private HashMap<String,String> responsedb = new HashMap<String,String>();

    private ArrayList<String> readLines(String filename) throws IOException   
    {  
        FileReader fileReader = new FileReader(filename);  
          
        BufferedReader bufferedReader = new BufferedReader(fileReader);  
        ArrayList<String> lines = new ArrayList<String>();  
        String line = null;  
          
        while ((line = bufferedReader.readLine()) != null)   
        {  
            lines.add(line);  
        }  
          
        bufferedReader.close();  
          
        return lines;
    }     

	public void onEnable() {
		try { //Attempt to load files
			for(String entry : this.readLines("responses.wst")){
				String[] parts = entry.split(",", 1);
				if(parts.length == 2) {
					responsedb.put(parts[0], parts[1]);
				}
			}
		} catch (IOException e) {
			
		}
		
		for(String key : responsedb.keySet()) {
			if("blah message".contains(key)) {
				responsedb.get(key);
			}
		}
	}
}
