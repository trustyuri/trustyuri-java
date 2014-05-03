package net.trustyuri;

public class TrustyUriException extends Exception {

	private static final long serialVersionUID = -3781230818163487604L;

	public TrustyUriException(String message) {
		super(message);
	}

	public TrustyUriException(Throwable cause) {
		super(cause);
	}

	public TrustyUriException(String message, Throwable cause) {
		super(message, cause);
	}

}
