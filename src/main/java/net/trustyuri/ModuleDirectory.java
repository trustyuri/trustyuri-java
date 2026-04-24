package net.trustyuri;

import net.trustyuri.file.FileModule;
import net.trustyuri.rdf.RdfGraphModule;
import net.trustyuri.rdf.RdfModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores all available modules.
 *
 * @author Tobias Kuhn
 */
public class ModuleDirectory {

    private static final Logger logger = LoggerFactory.getLogger(ModuleDirectory.class);

    private ModuleDirectory() {
    }  // no instances allowed

    private static final Map<String, TrustyUriModule> modules = new HashMap<>();

    static {
        addModule(new FileModule());
        addModule(new RdfModule());
        addModule(new RdfGraphModule());
        logger.debug("ModuleDirectory initialized with {} modules: {}", modules.size(), modules.keySet());
    }

    /**
     * Returns the module object for the given ID.
     *
     * @param moduleId the module ID
     * @return the module object
     */
    public static TrustyUriModule getModule(String moduleId) {
        TrustyUriModule module = modules.get(moduleId);
        if (module == null) {
            logger.debug("No module registered for ID: '{}'", moduleId);
        }
        return module;
    }

    private static void addModule(TrustyUriModule module) {
        modules.put(module.getModuleId(), module);
        logger.debug("Registered module '{}' ({})", module.getModuleId(), module.getClass().getSimpleName());
    }

}
