package net.trustyuri;

import org.openrdf.model.URI;

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

	public boolean matches(URI uri) {
		return uri.stringValue().matches(pattern);
	}

}
