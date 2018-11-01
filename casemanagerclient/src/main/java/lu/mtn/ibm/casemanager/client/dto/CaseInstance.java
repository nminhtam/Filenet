/**
 * 
 */
package lu.mtn.ibm.casemanager.client.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lu.mtn.ibm.filenet.client.dto.CEDocument;

/**
 * @author MTN
 *
 */
public class CaseInstance implements Serializable{
    
    /**
    *
    */
    private static final long serialVersionUID = 1L;
   
    protected Map<String, Object> properties;

    protected List<CEDocument> documents;

    private String caseType;
    
    private String caseId;
    
    private List<CaseTask> tasks;
    
    /**
     *
     */
    public CaseInstance() {
        this.properties = new HashMap<String, Object>();
        this.documents = new ArrayList<CEDocument>();
        this.tasks = new ArrayList<CaseTask>();
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

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * @return the documents
     */
    public List<CEDocument> getDocuments() {
        return this.documents;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Object> caseProperties) {
        this.properties = caseProperties;
    }

    /**
     * @param documents the documents to set
     */
    public void setDocuments(List<CEDocument> documents) {
        this.documents = documents;
    }

    /**
     * @return the tasks
     */
    public List<CaseTask> getTasks() {
        return this.tasks;
    }

    /**
     * @param tasks the tasks to set
     */
    public void setTasks(List<CaseTask> tasks) {
        this.tasks = tasks;
    }
}
