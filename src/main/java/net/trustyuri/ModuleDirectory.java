package net.trustyuri;

import java.util.HashMap;
import java.util.Map;

import net.trustyuri.file.FileModule;
import net.trustyuri.rdf.RdfGraphModule;
import net.trustyuri.rdf.RdfModule;


public class ModuleDirectory {

	private ModuleDirectory() {}  // no instances allowed

	private static Map<String,TrustyUriModule> modules = new HashMap<>();

	static {
		addModule(new FileModule());
		addModule(new RdfModule());
		addModule(new RdfGraphModule());
	}

	public static TrustyUriModule getModule(String moduleId) {
		return modules.get(moduleId);
	}

	private static void addModule(TrustyUriModule module) {
		modules.put(module.getModuleId(), module);
	}

}
