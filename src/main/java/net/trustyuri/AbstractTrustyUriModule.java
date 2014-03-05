package net.trustyuri;

import org.openrdf.model.URI;

public abstract class AbstractTrustyUriModule implements TrustyUriModule {

	private final String pattern;

	public AbstractTrustyUriModule() {
		pattern = ".*[^A-Za-z0-9\\-_]" + getModuleId() + "[A-Za-z0-9\\-_]{" + getDataPartLength() + "}";
	}

	public boolean matches(URI uri) {
		return uri.stringValue().matches(pattern);
	}

}
