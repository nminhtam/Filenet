/**
 *
 */
package lu.mtn.ibm.casemanager.client.dto;


/**
 * @author MTN
 *
 */
public class CaseManagerCreationRequest extends CaseManagerOperationRequest {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String caseType;


    /**
     *
     */
    public CaseManagerCreationRequest(String caseType) {
        this.caseType = caseType;
    }


    /**
     * @return the caseType
     */
    public String getCaseType() {
        return this.caseType;
    }

    /**
     * @param caseType the caseType to set
     */
    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }
}
