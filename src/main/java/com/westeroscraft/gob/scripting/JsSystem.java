package com.westeroscraft.gob.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.westeroscraft.gob.core.CorePlugin;
public class JsSystem {
	
	private File folder;
	private CorePlugin plugin;

	public JsSystem(File folder, CorePlugin plugin) {
		this.folder = folder;
		this.plugin = plugin;
	}
	
	public void include(Scriptable scope, String path) {
		Context ctx = Context.enter();
		try {
			ctx.evaluateReader(scope, new FileReader(new File(this.folder,path)), path, 0, null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//new ScriptableObject();
	};
	
	private class scheduledEvent implements Runnable {
		private Function f;
		private Object[] args;
		private Scriptable scope;
		private scheduledEvent(Function f, Object[] args, Scriptable scope) {
			this.f = f;
			this.scope = scope;
			this.args = args;
		}
		public void run() {
			Context ctx = Context.getCurrentContext();
			if(ctx!=null) {
				try {
				
				ctx.callFunctionWithContinuations(f,scope, args);
				} catch (ContinuationPending p) { if(p.getApplicationState() == JSFunctions.scopeflag) {p.setApplicationState(scope);}};
			}
		}
		
	}
	
	public void schedule(Scriptable scope, long delay, Function f, Object... args) {
		scheduledEvent e = new scheduledEvent(f, args, scope);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, e, delay);
	}
}
