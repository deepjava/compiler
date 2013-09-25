package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class Programmer extends Item {

	private HString description;
	private HString pluginId;
	private HString className;
	
	public Programmer(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public HString getDescription() {
		return this.description;
	}
	
	public void setClassName(String name) {
		this.className = HString.getRegisteredHString(name);
	}
	
	public HString getClassName() {
		return this.className;
	}
	
	public void setPluginId(String id) {
		this.pluginId = HString.getRegisteredHString(id);
	}
	
	public HString getPluginId() {
		return this.pluginId;
	}
	
}
