package net.trustyuri;

import org.eclipse.rdf4j.model.IRI;

/**
 * A partial implementation of a trusty URI module.
 *
 * @author Tobias Kuhn
 */
public abstract class AbstractTrustyUriModule implements TrustyUriModule {

	private final String pattern;

	/**
	 * Initializes this abstract class.
	 */
	public AbstractTrustyUriModule() {
		pattern = ".*[^A-Za-z0-9\\-_]" + getModuleId() + "[A-Za-z0-9\\-_]{" + getDataPartLength() + "}";
	}

	public boolean matches(IRI uri) {
		return uri.stringValue().matches(pattern);
	}

}
