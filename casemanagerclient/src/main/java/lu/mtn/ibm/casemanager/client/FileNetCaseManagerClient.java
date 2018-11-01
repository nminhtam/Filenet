/**
 * 
 */
package lu.mtn.ibm.casemanager.client;

import lu.mtn.ibm.casemanager.client.dto.CaseInstance;
import lu.mtn.ibm.casemanager.client.dto.CaseManagerCreationRequest;
import lu.mtn.ibm.casemanager.client.dto.CaseManagerModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;

/**
 * @author MTN
 *
 */
public interface FileNetCaseManagerClient {

    /**
     * 
     */
    public static final String CM_ACM_CASE_IDENTIFIER = "CmAcmCaseIdentifier";
    
    String startCase(CaseManagerCreationRequest request) throws ServiceCallException;
    
    String startCase(CaseManagerCreationRequest request, String objectStoreName) throws ServiceCallException;
    
    void updateCase(CaseManagerModificationRequest request) throws ServiceCallException;
    
    void updateCase(CaseManagerModificationRequest request, String objectStoreName) throws ServiceCallException;
    
    CaseInstance findCase(String caseId) throws ServiceCallException;
    
    CaseInstance findCase(String caseId, String objectStoreName) throws ServiceCallException;
    
    boolean existCase(String caseId) throws ServiceCallException;
    
    boolean existCase(String caseId, String objectStoreName) throws ServiceCallException;
}
