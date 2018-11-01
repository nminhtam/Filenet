/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.exception;

/**
 * @author NguyenT
 *
 */
public class OperationInitializationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public OperationInitializationException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public OperationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public OperationInitializationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OperationInitializationException(Throwable cause) {
        super(cause);
    }
}
