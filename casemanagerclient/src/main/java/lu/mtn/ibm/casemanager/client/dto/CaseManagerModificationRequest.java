/**
 *
 */
package lu.mtn.ibm.casemanager.client.dto;


/**
 * @author MTN
 *
 */
public class CaseManagerModificationRequest extends CaseManagerOperationRequest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String caseId;

    /**
     *
     */
    public CaseManagerModificationRequest(String caseId) {
        super();
        this.caseId = caseId;
    }

    /**
     * @return the caseId
     */
    public String getCaseId() {
        return this.caseId;
    }

    /**
     * @param caseId the caseId to set
     */
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
