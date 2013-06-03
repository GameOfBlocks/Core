package com.westeroscraft.gob.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_4_R1.NBTTagCompound;
import net.minecraft.server.v1_4_R1.NBTTagList;
import net.minecraft.server.v1_4_R1.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import com.westeroscraft.gob.menu.Menu;
import com.westeroscraft.gob.menu.TeleportSpawnButton;
import com.westeroscraft.gob.menu.TestToggleButton;
import com.westeroscraft.gob.scripting.JSFunctions;
import com.westeroscraft.gob.scripting.JsSystem;
import com.westeroscraft.gob.shipping.PendingPackage;
import com.westeroscraft.gob.shipping.ShippableInventory;


public class CorePlugin extends JavaPlugin implements Listener {
	public HashMap<NPC, ScriptableObject> registry = new HashMap<NPC, ScriptableObject>();
	public WeakHashMap<Player,Scriptable> playerData = new WeakHashMap<Player,Scriptable>();
	public HashSet<String> questItems = new HashSet<String>();
	public ScriptableObject scope;
	
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		CitizensAPI.getNPCRegistry().deregisterAll();
		
		this.getLogger().log(Level.INFO, "Loading npcs");
		Context cx = Context.enter();
		cx.setOptimizationLevel(-1); //must run in an interpreted mode
		scope = cx.initStandardObjects();
		Object utilFunctions = Context.javaToJS(new JSFunctions(this), scope);
		Object sysFunctions = Context.javaToJS(new JsSystem(this.getDataFolder(),this), scope);
		ScriptableObject.putProperty(scope, "util", utilFunctions);
		ScriptableObject.putProperty(scope, "sys", sysFunctions);
		new File(this.getDataFolder(),"data").mkdirs();
		try {
			cx.executeScriptWithContinuations(cx.compileReader(new FileReader(new File(this.getDataFolder(), "core.js")), "core", 0, null), scope);
		} catch(ContinuationPending p) {
			p.setApplicationState(scope);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.getLogger().log(Level.INFO, "Finsihed loading NPCs");
		
		
	}
	
	public void onDisable() {
		CitizensAPI.getNPCRegistry().deregisterAll();
		for(Entry<Player, Scriptable>  e: playerData.entrySet()) {
			this.savePlayerData(e.getKey().getName(),e.getValue());
		}
		Context.exit();
	}
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e) {
		Scriptable data = playerData.get(e.getPlayer());
		if(data != null) {
			this.savePlayerData(e.getPlayer().getName(), data);
		}
	}
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent e) {
		if(!CorePlugin.getQuestTag(e.getItemDrop().getItemStack()).isEmpty()){
			e.getItemDrop().remove();
		}
	}
	@EventHandler
	public void onDeathEvent(PlayerDeathEvent e) {
		Iterator<ItemStack> itr = e.getDrops().iterator();
		while(itr.hasNext()) {
			ItemStack is = itr.next();
			if(!CorePlugin.getQuestTag(is).isEmpty()){
				itr.remove();
			}
		}
	}
	
	

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent e) {
		System.out.println("Click!" + e.getRawSlot());
		if(e.getView().getType() == InventoryType.CRAFTING) {
			if((e.getCursor() == null || e.getCursor().getType() == Material.AIR) && e.getRawSlot() == InventoryView.OUTSIDE){
				Menu inv = getMenu(e.getWhoClicked());
				this.pushInventory(e.getWhoClicked(), e.getInventory(), true);
				openMenu(inv, e.getWhoClicked());
			}
		}
		
		System.out.println("Debug state: clicker hash " + e.getWhoClicked().hashCode() + ", inventory" + ((CraftInventory)e.getView().getTopInventory()).getInventory());
		if(((CraftInventory)metashipping.get(e.getWhoClicked()).getInventory()).getInventory() == ((CraftInventory)e.getView().getTopInventory()).getInventory()) {
			metashipping.get(e.getWhoClicked()).onInventoryClick(e);
		}
	}
	
	
	
	
	private Menu getMenu(HumanEntity e) {
		Menu m = metashipping.get(e);
		if(m == null) {
			CraftInventory inv = (CraftInventory) this.getServer().createInventory(e, 27,"Game Menu");
			
			m = new Menu(inv);
			m.addMenuItem(new TestToggleButton());
			m.addMenuItem(new TeleportSpawnButton());
			metashipping.put(e, m);
		}
		return m;
	}

	private void openMenu(Menu m, HumanEntity e) {
		m.rerender();
		e.openInventory(m.getInventory());
	}

	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent e) {
		System.out.println("INV: " + e.getView().getType().name());
		if(e.getView().getType() == InventoryType.CRAFTING) {

		}
	}
	@EventHandler
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		System.out.println("INV CLOSE: " + e.getView().getType().name());
		if(e.getView().getType() == InventoryType.CRAFTING) {

		}
		if(e.getPlayer()  instanceof Player) {
			Player  p = (Player) e.getPlayer();
			Stack<Inventory> s = currentinv.get(p);
			if(s != null) {
				if(!s.isEmpty()) {
					Inventory inv = s.pop();
					if(inv != null) {
						p.openInventory(inv);
					}
				}
			}
		}
	}
	
	private void pushInventory(HumanEntity e, Inventory i, boolean buffer) {
		//Remember the previous inventory
		if(i != null) {
			Stack<Inventory> s = currentinv.get(e);
			if(s == null){
				s = new Stack<Inventory>();
				currentinv.put(e, s);
			}
			s.push(i);
			if(buffer) { s.push(null); } // Buffer the close event from opening the new inventory
		}
	}
	
	
	private HashMap<HumanEntity, Menu> metashipping = new HashMap<HumanEntity,Menu>();
	private WeakHashMap<HumanEntity, Stack<Inventory>> currentinv = new WeakHashMap<HumanEntity,Stack<Inventory>>();
	
	/*private void openShipping(HumanEntity h, Inventory inventory) {
		if(h instanceof Player) {
			Player p = (Player) h;
			Inventory inv = metashipping.get(p);
			if(inv == null) { //Create the virtual inventory
				inv = this.getServer().createInventory(p, 27,"Shipping");
				metashipping.put(p, inv);
			}
			
			//Remember the previous inventory
			if(inventory != null) {
				Stack<Inventory> s = currentinv.get(p);
				if(s == null){
					s = new Stack<Inventory>();
					currentinv.put(p, s);
				}
				s.push(inventory);
				s.push(null); // Buffer the close event from opening the new inventory
			}
			//Rebuild virtual chest
			//inv.clear();
			CraftItemStack itm = new CraftItemStack(Material.CHEST);
			CorePlugin.setName(itm, "Wintefell");
			inv.addItem(itm);
			p.openInventory(inv);
			
		}
	}*/

	@EventHandler
	public void onNPCRightClickEvent(NPCRightClickEvent e) {
		ScriptableObject n = registry.get(e.getNPC());
		Object args[] = {e.getClicker(), e.getNPC()};
		if(n != null) {
			this.callNPCJSfunction(n,"onClick", args);
		}
		
	}
	@EventHandler
	public void onNPCDamageEvent(NPCDamageByEntityEvent e) {
		ScriptableObject n = registry.get(e.getNPC());
		if(e.getDamager() instanceof Player) {
			Object args[] = {(Player)e.getDamager(), e.getDamage(), e.getNPC()};
			if(n != null) {
				this.callNPCJSfunction(n,"onDamaged", args);
			}
		}

	}
	public void callNPCJSfunction(ScriptableObject n,String name, Object[] args) {
		Context ctx = Context.getCurrentContext();
		if(ctx!=null) {
			Object fObj = ScriptableObject.getProperty(n, name);
			if(fObj instanceof Function) {
				Function f = (Function) fObj;
				try {
				ctx.callFunctionWithContinuations(f, n, args);
				} catch (ContinuationPending p) { if(p.getApplicationState() == JSFunctions.scopeflag) {p.setApplicationState(n);}};
			}
		}
	}
	
	private static Field hiddenhandle;
	static {
		try {
			hiddenhandle = CraftItemStack.class.getDeclaredField("handle");
			hiddenhandle.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	
	public static net.minecraft.server.v1_4_R1.ItemStack getHandle(ItemStack s) {
		try {
			
			net.minecraft.server.v1_4_R1.ItemStack stack = (net.minecraft.server.v1_4_R1.ItemStack) hiddenhandle.get(s);
			if(stack == null) {
				stack = CraftItemStack.asNMSCopy(s);
				hiddenhandle.set(s, stack);
			}
			return stack;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String getQuestTag(ItemStack item) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(mcItem == null) {
			return "";
		}
		if(mcItem.hasTag()) {
			return mcItem.getTag().getString("QuestItem");
		} else {
			return "";
		}
	}
	public static boolean isPlayerBound(ItemStack item, String p) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(mcItem == null) {
			return false;
		}
		if(mcItem.hasTag()) {
			String b = mcItem.getTag().getString("bound");
			if(b == p || b.isEmpty()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	public static String getBoundPlayer(ItemStack item) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(mcItem == null) {
			return "";
		}
		if(mcItem.hasTag()) {
			String b = mcItem.getTag().getString("bound");
			return b;
		} else {
			return "";
		}
	}
	public static void setQuestTag(ItemStack item, String s) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setString("QuestItem", s);
	}
	public static void setPlayerBound(ItemStack i, String p) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setString("bound", p);
	}
	public static void setName(ItemStack i, String name) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		if(!mcItem.getTag().hasKey("dispaly")) {
			mcItem.getTag().set("display", new NBTTagCompound());
		}
		
		mcItem.getTag().getCompound("display").setString("Name", name);
	}
	public static String getName(ItemStack i) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(mcItem == null) {
			return null;
		}
		if(!mcItem.hasTag()) {
			return null;
		}
		if(!mcItem.getTag().hasKey("dispaly")) {
			return null;
		}
		if(!mcItem.getTag().getCompound("display").hasKey("Name")) {
			return null;
		}
		return mcItem.getTag().getCompound("display").getString("Name");
	}
	public static void setBook(ItemStack item, String author, String title, String[] pages){
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setString("author", author);
		mcItem.getTag().setString("title", title);
		mcItem.getTag().set("pages", new NBTTagList());
		for(String page : pages) {
			NBTTagString tag = new NBTTagString("", page);
			mcItem.getTag().getList("pages").add(tag);
		}
	}
	
	HashMap<Integer,PendingPackage> pendingpkgs = new HashMap<Integer,PendingPackage>();
	public ItemStack packageItem(ShippableInventory source, ItemStack packagable, ShippableInventory destination) {
		
		if(getPackageId(packagable) == 0 || getQuestTag(packagable).isEmpty()) {
			return null;
		}
		PendingPackage p = PendingPackage.generateUniquePackage(pendingpkgs.values());
		pendingpkgs.put(p.ID, p);
		p.destination = destination;
		p.source = source;
		CorePlugin.setPackageAmount(packagable, packagable.getAmount());
		packagable.setAmount(1);
		CorePlugin.setPakageSource(packagable, true);
		CorePlugin.setPackageID(packagable, p.ID);
		
		CraftItemStack pkg = CraftItemStack.asCraftCopy(new ItemStack(Material.JUKEBOX));
		CorePlugin.setPackageAmount(pkg, CorePlugin.getPakageAmount(packagable));
		CorePlugin.setPackageID(pkg, p.ID);
		
		this.describePackage(pkg);
		this.describePackage(packagable);
		//net.minecraft.server.v1_4_R1.ItemStack mcItem = ((CraftItemStack) pkg).getHandle();
		
		return pkg;
	}
	public void describePackage(ItemStack i) {
		if(CorePlugin.getPackageId(i) != 0) {
			if(CorePlugin.isPakageSource(i)) {
				String[] lore = {"Item is shipping"};
				CorePlugin.setLore(i, lore);
			} else {
				PendingPackage pend = this.pendingpkgs.get(CorePlugin.getPackageId(i));

				if(pend != null) {
					ItemStack packable = findByPackageId(CorePlugin.getPackageId(i),pend.source.inventory);
					String name = getName(packable);
					if(name == null) {
						name = packable.getType().name().toLowerCase();
					}
					setName(i, "Pending Package for " + name);
					String[] lore = {"Item is being shipped from: " + pend.source.name,
							"Progress: " + pend.progress + "/" + pend.required};
					setLore(i,lore);
				} else {
					setName(i, "Pending Package");
				}
			}

		}

		
	}
	
	public ItemStack findByPackageId(int id, Inventory i) {
		PendingPackage p = pendingpkgs.get(id);
		if(p.ID != id) { //Should never happen, so remove the error
			pendingpkgs.remove(id);
			p = null;
		}
		ItemStack itm = null;
		
		if(p != null) {
			ListIterator<ItemStack> itr = p.source.inventory.iterator();
			while(itr.hasNext()) {
				ItemStack tmp = itr.next();
				if(getPackageId(tmp) == id) {
					itm = tmp;
					break;
				}
			}
		}
		
		return itm;
	}
	public static int getPackageId(ItemStack i) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(mcItem == null) {
			return 0;
		}
		if(mcItem.hasTag()) {
			return mcItem.getTag().getInt("package-id");
		} else {
			return 0;
		}
	}
	public static boolean isPakageSource(ItemStack i) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(mcItem == null) {
			return false;
		}
		if(mcItem.hasTag()) {
			return mcItem.getTag().getBoolean("package-source");
		} else {
			return false;
		}
	}
	public static void setPakageSource(ItemStack i, boolean source) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setBoolean("package-id", source);
	}
	public static int getPakageAmount(ItemStack i) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(i);
		if(mcItem == null) {
			return 0;
		}
		if(mcItem.hasTag()) {
			return mcItem.getTag().getInt("package-amount");
		} else {
			return 0;
		}
	}
	
	public static void setPackageID(ItemStack item, int id) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setInt("package-id", id);
	}
	public static void setPackageAmount(ItemStack item, int id) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		mcItem.getTag().setInt("package-amount", id);
	}
	
	public static void setLore(ItemStack item, String[] lore) {
		net.minecraft.server.v1_4_R1.ItemStack mcItem = getHandle(item);
		if(!mcItem.hasTag()) {
			mcItem.setTag(new NBTTagCompound());
		}
		if(!mcItem.getTag().hasKey("display")) {
			mcItem.getTag().set("display", new NBTTagCompound());
		}
		
		
		NBTTagList l = new NBTTagList();
		for(int i = 0 ; i < lore.length; i++) {
			l.add(new NBTTagString("" + i, lore[i]));
		}
		mcItem.getTag().getCompound("display").set("Lore", l);
	}
	public static void describe(ItemStack item) {
		
		//net.minecraft.server.v1_4_R1.ItemStack mcItem = ((CraftItemStack) item).getHandle();
		String qtag = getQuestTag(item);
		String btag = getBoundPlayer(item);
		if(qtag.isEmpty()) {
			if(!btag.isEmpty()) {
				String[] lore = {"owned by " + btag};
				setLore(item, lore);
			}
		} else {
			if(btag.isEmpty()) {
				String[] lore = {"Quest Item"};
				setLore(item, lore);
			} else {
				String[] lore = {"Quest Item for " + btag};
				setLore(item,lore);
			}
		}
		
		
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			sendQuestLog((Player)sender);
		}
		return true;
	}
	public ItemStack updateQuestLog(Player p) {
		Scriptable data = this.loadPlayerData(p.getName());
		HashMap<Integer, ? extends ItemStack> book = p.getInventory().all(Material.WRITTEN_BOOK);
		ItemStack b = null;
		boolean has = false;
		ArrayList<Integer> duplicates = new ArrayList<Integer>();
		for(Entry<Integer, ? extends ItemStack> e: book.entrySet()) {
			if(getQuestTag(e.getValue()).equals("log")) {
				if(has) {
					duplicates.add(e.getKey());
					continue;
				}
				has = true;
				b = e.getValue();			
			}
		}
		
		//Remove duplicate quest logs
		for(Integer i : duplicates) {
			p.getInventory().setItem(i.intValue(), null);
		}
		
		if(b == null) {
			b = CraftItemStack.asCraftCopy(new ItemStack(Material.WRITTEN_BOOK));
		}
		
		int lines = 0;
		int chars = 0;
		String currentPage = "";
		ArrayList<String> pages = new ArrayList<String>();
		//Begin parsing the quest log data from the player persistance
		Object obj = data.get("log", data);
		if(obj instanceof NativeArray) {
			System.out.println("found an array");
			NativeArray ray = (NativeArray)obj;
			int l = (int) ray.getLength();
			for(int i = 0; i<l; i++) {
				Object el = ray.get(i, ray);
				System.out.println("found a " + el.getClass().getSimpleName());
				if( el instanceof NativeObject) {
					NativeObject nobj = (NativeObject)el;
					Object note = ScriptableObject.getProperty(nobj, "note");
					 
					if( note instanceof String) {
						System.out.println("Found a note! " + (String)note);
						int notelines = 0;
						String str = (String) note;
						String[] words = str.split(" ");
						for(String word : words) {
							chars += word.length();
							if(chars > 19) {
								chars -= 19;
								notelines++;
							}
						}
						if(lines + notelines > 13) {
							pages.add(new String(currentPage));
							currentPage = "";
							lines = 0;
							chars = 0;
						}
						currentPage += str;
						currentPage += "\n" + ChatColor.BLACK +"\n";
						lines += notelines + 2;
					}
				}
			}
		}
		if(!currentPage.isEmpty()) {
			pages.add(currentPage);
		}
		if(pages.size() == 0) {
			pages.add("You have no notes");
		}
		CorePlugin.setBook(b, p.getName(), "Quest Log", pages.toArray(new String[0]));
		CorePlugin.setQuestTag(b, "log");
		String[] lore = { "A collection of notes" }; 
		CorePlugin.setLore(b, lore);
		return b;
	}
	public void sendQuestLog(Player p) {
		HashMap<Integer, ? extends ItemStack> book = p.getInventory().all(Material.WRITTEN_BOOK);
		boolean has = false;
		for(Entry<Integer, ? extends ItemStack> e: book.entrySet()) {
			if(getQuestTag(e.getValue()).equals("log")) {
				has = true;	
			}
		}
		ItemStack s = this.updateQuestLog(p);
		if(!has && p.getInventory().addItem(s).size() > 0) {
			p.sendMessage("You don't have any inventory space to place your quest log");
		}
	}
	static String join(String[] s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     for(int i = 0; i < s.length; i++) {
	         builder.append(s[i]);
	         if (i +1 < s.length) {
		         builder.append(delimiter);
	         }
	     }
	     return builder.toString();
	 }
	
	public Scriptable loadPlayerData(String s) {
		Player p = this.getServer().getPlayerExact(s);
		Scriptable data = null;
		if(p != null) {
			data = this.playerData.get(p);
		}
		if(data == null) {
			ScriptableInputStream inputs;
			Object o = null;
			try {
				inputs = new ScriptableInputStream(new FileInputStream(new File(this.getDataFolder(),"data/"+s+".dat")), scope);
				o = inputs.readObject();
				inputs.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if(o != null) {
				this.getLogger().log(Level.INFO,"Loaded player data for " + s + " and returns an object of type: " + o.getClass().getSimpleName());
			}
			if(o instanceof Scriptable) {
				if(p != null) {
					this.playerData.put(p, (Scriptable)o);
				}
				return (Scriptable)o;
			} else {
				NativeObject obj = new NativeObject();
				if(p != null) {
					this.playerData.put(p, obj);
				}
				return obj;
			}
		} else {
			return data;
		}
	}
	
	
	public void savePlayerData(String s, Scriptable data) {
		
		try {
			ScriptableOutputStream outs = new ScriptableOutputStream(new FileOutputStream(new File(this.getDataFolder(),"data/"+s+".dat")), scope);
			outs.writeObject(data);
			outs.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
