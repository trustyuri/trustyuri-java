package ch.tkuhn.hashuri;

import java.util.HashMap;
import java.util.Map;

import ch.tkuhn.hashuri.file.FileModule;
import ch.tkuhn.hashuri.rdf.RdfModule;

public class ModuleDirectory {

	private ModuleDirectory() {}  // no instances allowed

	private static Map<String,HashUriModule> modules = new HashMap<>();

	static {
		addModule(new RdfModule());
		addModule(new FileModule());
	}

	public static HashUriModule getModule(String algorithmID) {
		return modules.get(algorithmID);
	}

	private static void addModule(HashUriModule module) {
		modules.put(module.getAlgorithmID(), module);
	}

}
