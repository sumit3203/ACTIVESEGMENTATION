package activeSegmentation.gui;

import java.util.HashMap;

import ijaux.datatype.Pair;

public class SettingsDB {

	public SettingsDB() {
		// TODO Auto-generated constructor stub
	}
	
	HashMap<String, Pair<String, String>> settdb= new HashMap<>();
	
	Pair<String, String> get(String key) {
		return settdb.get(key);
	}
	
	public void addSetting(String key, String var, String val) {
		settdb.putIfAbsent(key, Pair.of(var,val));
	}
	
	public void updateSetting(String key, String var, String val) {
		settdb.put(key, Pair.of(var,val));
	}
	

}
