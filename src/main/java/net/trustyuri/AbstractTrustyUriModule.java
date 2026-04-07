package net.trustyuri;

import org.eclipse.rdf4j.model.IRI;

/**
 * A partial implementation of a trusty URI module.
 *
 * @author Tobias Kuhn
 */
public abstract class AbstractTrustyUriModule implements TrustyUriModule {

    private final String pattern;
    private final String moduleId;

    /**
     * Constructs an AbstractTrustyUriModule with the given module ID.
     *
     * @param moduleId the module ID
     */
    protected AbstractTrustyUriModule(String moduleId) {
        pattern = ".*[^A-Za-z0-9\\-_]" + getModuleId() + "[A-Za-z0-9\\-_]{" + getDataPartLength() + "}";
        this.moduleId = moduleId;
    }

    /**
     * Checks if the given URI matches the pattern for this module.
     *
     * @param uri the URI to check
     * @return true if the URI matches the pattern, false otherwise
     */
    public boolean matches(IRI uri) {
        return uri.stringValue().matches(pattern);
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
