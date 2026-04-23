package net.trustyuri;

/**
 * Exception thrown by the trustyuri library.
 */
public class TrustyUriException extends Exception {

    private static final long serialVersionUID = -3781230818163487604L;

    /**
     * Creates a new instance of <code>TrustyUriException</code> without detail message.
     *
     * @param message the detail message.
     */
    public TrustyUriException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of <code>TrustyUriException</code> with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public TrustyUriException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of <code>TrustyUriException</code> with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public TrustyUriException(String message, Throwable cause) {
        super(message, cause);
    }

}
