/**
 *
 */
package lu.mtn.ibm.filenet.client.exception;

/**
 * @author NguyenT
 *
 */
public class ServiceCallException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ServiceCallException() {
        super();
    }

    public ServiceCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceCallException(String message) {
        super(message);
    }

    public ServiceCallException(Throwable cause) {
        super(cause);
    }

}
