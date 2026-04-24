package net.trustyuri;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A partial implementation of a trusty URI module.
 *
 * @author Tobias Kuhn
 */
public abstract class AbstractTrustyUriModule implements TrustyUriModule {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTrustyUriModule.class);

    private final String pattern;
    private final String moduleId;

    /**
     * Constructs an AbstractTrustyUriModule with the given module ID.
     *
     * @param moduleId the module ID
     */
    protected AbstractTrustyUriModule(String moduleId) {
        this.moduleId = moduleId;
        pattern = ".*[^A-Za-z0-9\\-_]" + getModuleId() + "[A-Za-z0-9\\-_]{" + getDataPartLength() + "}";
        logger.debug("Initialized module '{}' with URI pattern: {}", moduleId, pattern);
    }

    /**
     * Checks if the given URI matches the pattern for this module.
     *
     * @param uri the URI to check
     * @return true if the URI matches the pattern, false otherwise
     */
    public boolean matches(IRI uri) {
        boolean result = uri.stringValue().matches(pattern);
        logger.debug("URI '{}' {} pattern for module '{}'", uri.stringValue(), result ? "matches" : "does not match", moduleId);
        return result;
    }

    /**
     * Returns the module ID of this module.
     *
     * @return the module ID
     */
    public String getModuleId() {
        return this.moduleId;
    }

}
