/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.exception;

/**
 * @author NguyenT
 *
 */
public class WriteException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public WriteException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public WriteException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public WriteException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public WriteException(Throwable cause) {
        super(cause);
    }


}
