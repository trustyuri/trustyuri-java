package net.trustyuri;

import java.util.HashMap;
import java.util.Map;

import net.trustyuri.file.FileModule;
import net.trustyuri.rdf.RdfGraphModule;
import net.trustyuri.rdf.RdfModule;

/**
 * This class stores all available modules.
 *
 * @author Tobias Kuhn
 */
public class ModuleDirectory {

	private ModuleDirectory() {}  // no instances allowed

	private static Map<String,TrustyUriModule> modules = new HashMap<>();

	static {
		addModule(new FileModule());
		addModule(new RdfModule());
		addModule(new RdfGraphModule());
	}

	/**
	 * Returns the module object for the given ID.
	 *
	 * @param moduleId the module ID
	 * @return the module object
	 */
	public static TrustyUriModule getModule(String moduleId) {
		return modules.get(moduleId);
	}

	private static void addModule(TrustyUriModule module) {
		modules.put(module.getModuleId(), module);
	}

}
