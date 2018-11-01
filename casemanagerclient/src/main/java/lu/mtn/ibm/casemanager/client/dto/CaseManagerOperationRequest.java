/**
 *
 */
package lu.mtn.ibm.casemanager.client.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lu.mtn.ibm.filenet.client.dto.DocumentOperationRequest;

/**
 * @author NguyenT
 *
 */
public abstract class CaseManagerOperationRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Map<String, Object> caseProperties;

    protected List<DocumentOperationRequest> documents;

    /**
     *
     */
    public CaseManagerOperationRequest() {
        this.caseProperties = new HashMap<String, Object>();
        this.documents = new ArrayList<DocumentOperationRequest>();
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getCaseProperties() {
        return this.caseProperties;
    }

    /**
     * @return the documents
     */
    public List<DocumentOperationRequest> getDocuments() {
        return this.documents;
    }

    /**
     * @param properties the properties to set
     */
    public void setCaseProperties(Map<String, Object> caseProperties) {
        this.caseProperties = caseProperties;
    }

    /**
     * @param documents the documents to set
     */
    public void setDocuments(List<DocumentOperationRequest> documents) {
        this.documents = documents;
    }
}
