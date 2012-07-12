package com.westeroscraft.gob.core;

import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class GenericUnit implements Unit {
	private Set<Unit> children;
	private Unit parent;
	private String name;
	
	private HashMap<Class<? extends Property>,Property> props = 
		new HashMap<Class<? extends Property>,Property>();
	
	protected static final EnumSet<Capabilities> capability = EnumSet.of(
			Capabilities.Child,
			Capabilities.ExclusiveOwner,
			Capabilities.Parent,
			Capabilities.Searchable);
	
	public GenericUnit() {
		children = new HashSet<Unit>();
		parent = null;
		name = new String();
	}
	
	public GenericUnit(String name) {
		this();
		this.name = name;
	}
	
	public EnumSet<Capabilities> getCapabilities() {
		return GenericUnit.capability;
	}

	public Unit[] getChildren() {
		return children.toArray(new Unit[0]);
	}

	public void addChild(Unit... unit) {
		EnumSet<Capabilities> cap = this.getCapabilities();
		if(cap.contains(Capabilities.Parent)) {
			return; //This unit does not support being a parent
		}
		
		//Check if if this unit enforces strict hierarchy
		boolean exclusive = (cap.contains(Capabilities.ExclusiveOwner)) ? true : false;
		
		//Iterate of the pending units and swap parents if needed
		for(Unit u : unit) {
			if(u == null) {
				continue;
			}
			if(u.getCapabilities().contains(Capabilities.Child)) {
				//Ensure no hierarchical looping
				if(u.containsUnit(this, -1)) {
					continue;
				}

				Unit oldparent = u.getParent();
				
				//Set the parent of unit if not previously defined or when this unit is an exclusive owner
				if(oldparent == null || exclusive) {
					u.setParent(this);
				}
				
				//Actually add the unit as a child
				children.add(u);
			}
		}
	}

	public void removeChild(Unit... unit) {
		for(Unit u : unit) {
			children.remove(u);
		}
	}

	public void removeChildren() {
		if(getCapabilities().contains(Capabilities.ExclusiveOwner)) {
			Set<Unit> safechildren = new HashSet<Unit>();  //Prevent locking issues
			safechildren.addAll(children);
			for(Unit child : safechildren) {
				child.setParent(null);
			}
		}
		//For exclusive units this *should* do nothing
		children.clear();
	}
	
	public Unit getParent() {
		return parent;
	}

	@SuppressWarnings("unchecked")
	public <T extends Unit> T[] findUnits(Class<T> u, int depth) {
		HashSet<T> units = new HashSet<T>();
		if(!this.getCapabilities().contains(Capabilities.Searchable)){
			return units.toArray((T[]) Array.newInstance(u,0));
		}
		for(Unit unit : children) {
			if(u.isInstance(unit)) {
				units.add((T) unit);
			}
			if(depth != 0) {
				for(T subunits : unit.findUnits(u, depth -1)) {
					units.add(subunits);
				}
			}
		}
		return units.toArray((T[]) Array.newInstance(u,0));
	}

	public String getName() {
		return name;
	}

	public void setParent(Unit u) {
		//Remove the previous parent's relationship if old parents are exclusive
		if(parent != null && parent.getCapabilities().contains(Capabilities.ExclusiveOwner)) {
			parent.removeChild(u);
		}
		parent = u;
	}

	public boolean containsUnit(Unit u, int depth) {
		if(children.contains(u)) {
			return true;
		}
		if(depth != 0) {
			for(Unit child : children) {
				if(child.containsUnit(u, depth-1)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addProperty(Property... p) {
		for(Property prop : p) {
			this.props.put(prop.getClass(), prop);
		}
	}

	@SuppressWarnings("unchecked")
	public <P extends Property> P getProperty(Class<P> property) {
		return (P) this.props.get(property);
	}

}