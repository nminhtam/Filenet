/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.exception;

/**
 * @author NguyenT
 *
 */
public class OperationExecutionException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public OperationExecutionException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public OperationExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public OperationExecutionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OperationExecutionException(Throwable cause) {
        super(cause);
    }
}
