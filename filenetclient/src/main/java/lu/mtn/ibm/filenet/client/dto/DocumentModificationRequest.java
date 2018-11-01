/**
 *
 */
package lu.mtn.ibm.filenet.client.dto;


/**
 * @author MTN
 *
 */
public class DocumentModificationRequest extends DocumentOperationRequest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String documentId;

    /**
     *
     */
    public DocumentModificationRequest(String documentId) {
        super();
        this.documentId = documentId;
    }

    /**
     * @return the documentId
     */
    public String getDocumentId() {
        return this.documentId;
    }

    /**
     * @param documentId the documentId to set
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
