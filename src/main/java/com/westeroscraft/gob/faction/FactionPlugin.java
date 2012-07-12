package com.westeroscraft.gob.faction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.HashMap;

public class FactionPlugin extends JavaPlugin implements CommandHandler {
	private final static String DELIM = "*:*";
	HashMap<String,MethodPair> playerCMDhandler;
	HashMap<String,MethodPair> genericCMDhandler;
	@Command({"log","cake"})
	public void onEnable() {
		
		
	}
	
	
	
	
	public void addCommandHandler(CommandHandler handler, Class<? extends CommandSender> sender) {
		if(sender == Player.class) {
			for(Method m : handler.getClass().getMethods()){
				Command c = m.getAnnotation(Command.class);
				String s = FactionPlugin.join(c.value(),DELIM);
				playerCMDhandler.put(s, new MethodPair(m,handler));
			}
		} else {
			for(Method m : handler.getClass().getMethods()){
				Command c = m.getAnnotation(Command.class);
				String s = FactionPlugin.join(c.value(),DELIM);
				genericCMDhandler.put(s, new MethodPair(m,handler));
			}
		}
	}
	public static String join(String[] strCollection, String delimiter) {
	    String joined = "";
	    int noOfItems = 0;
	    for (String item : strCollection) {
	        joined += item;
	        if (++noOfItems < strCollection.length)
	            joined += delimiter;
	    }
	    return joined;
	}
	public static String join(String[] strCollection, String delimiter,int shrt) {
	    String joined = "";
	    int noOfItems = 0;
	    for (int i = 0; i < shrt; i++) {
	        joined += strCollection[i];
	        if (++noOfItems < shrt)
	            joined += delimiter;
	    }
	    return joined;
	}
	
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
		boolean isplayer = (sender instanceof Player) ? true : false;
		
		//Attempt to run the command through as player only then fall back on generic
		if(isplayer) {
			for(int i = args.length; i >= 0; i--) {
				String combined = join(args,DELIM,i);
				MethodPair mpair = this.playerCMDhandler.get(combined);
				try {
					mpair.m.invoke(mpair.o, (Player) sender, args);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		for(int i = args.length; i >= 0; i--) {
			String combined = join(args,DELIM,i);
			MethodPair mpair = this.playerCMDhandler.get(combined);
			try {
				mpair.m.invoke(mpair.o, (Player) sender, args);
			} catch (Exception e) { e.printStackTrace(); }
		}
		return false;
	}
}
