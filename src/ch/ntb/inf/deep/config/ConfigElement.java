package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class ConfigElement {
	
	private static final boolean dbg = false;

	public ConfigElement next;
	public ConfigElement prev;
	
	public HString name;
	
	public ConfigElement() {}
	
	public ConfigElement(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void insertAfter(ConfigElement itm) {
		if(this.next != null) {
			itm.next = this.next;
			this.next.prev = itm;
		}
		this.next = itm;
		itm.prev = this;
	}
	
	public void insertBefore(ConfigElement itm) {
		if(this.prev != null) {
			itm.prev = this.prev;
			this.prev.next = itm;
		}
		this.prev = itm;
		itm.next = this;
	}
	
	public void append(ConfigElement itm) {
		this.getTail().insertAfter(itm);
	}
		
	public ConfigElement getTail() {
		ConfigElement tail = this;
		while(tail.next != null) tail = tail.next;
		return tail;
	}
	
	public ConfigElement getHead() {
		ConfigElement head = this;
		while(head.prev != null) head = head.prev;
		return head;
	}
	
	public ConfigElement getElementByName(HString name) {
		if(dbg) StdStreams.vrb.println("[CONF] ConfigElement: looking for element " + name  + "(0x" + Integer.toHexString(name.hashCode()) + ")");
		ConfigElement e = this.getHead();
		if(dbg) StdStreams.vrb.print("  Starting with " + e.getName() + "(0x" + Integer.toHexString(e.getName().hashCode()) + ")");
		while(e != null && name != e.name) {
			e = e.next;
			if(dbg) StdStreams.vrb.print(" -> skip, next: ");
			if(dbg) if(e != null) StdStreams.vrb.print(e.getName() + "(0x" + Integer.toHexString(e.getName().hashCode()) + ")"); else StdStreams.vrb.print("<null>");
		}
		if(dbg) {
			if(e != null) {
				StdStreams.vrb.println(" -> found: " + e.getName());
			}
			else {
				StdStreams.vrb.println(" -> not found");
			}
		}
		return e;
	}
	
	public ConfigElement getElementByName(String jname) {
		HString name = HString.getRegisteredHString(jname);
		return this.getElementByName(name);
	}
	
	public HString getName(){
		return name;
	}
	
	public int getNofElements() {
		ConfigElement e = this.getHead();
		int c = 0;
		while(e != null) {
			c++;
			e = e.next;
		}
		return c;
	}
	
	public void replace(ConfigElement newElement) {
		if(this.prev != null) {
			this.prev.next = newElement;
			newElement.prev = this.prev;
		}
		
		if(this.next != null) {
			this.next.prev = newElement;
			newElement.next = this.next;
		}
		
		this.prev = null;
		this.next = null;
	}
}
