/**
 *
 */
package lu.mtn.ibm.filenet.client.dto;


/**
 * @author MTN
 *
 */
public class DocumentCreationRequest extends DocumentOperationRequest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String documentClassName;

    private String folder;




    /**
     *
     */
    public DocumentCreationRequest(String documentClassName) {
        this.documentClassName = documentClassName;
    }

    /**
     * @return the documentClassName
     */
    public String getDocumentClassName() {
        return this.documentClassName;
    }

    /**
     * @param documentClassName the documentClassName to set
     */
    public void setDocumentClassName(String documentClassName) {
        this.documentClassName = documentClassName;
    }

    /**
     * @return the folder
     */
    public String getFolder() {
        return this.folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }
}
