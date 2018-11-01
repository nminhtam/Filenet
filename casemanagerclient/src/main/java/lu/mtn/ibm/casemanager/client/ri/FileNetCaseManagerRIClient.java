/**
 * 
 */
package lu.mtn.ibm.casemanager.client.ri;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Property;
import com.ibm.casemgmt.api.Case;
import com.ibm.casemgmt.api.CaseType;
import com.ibm.casemgmt.api.constants.CaseState;
import com.ibm.casemgmt.api.constants.ErrorCategory;
import com.ibm.casemgmt.api.constants.ModificationIntent;
import com.ibm.casemgmt.api.exception.CaseMgmtException;
import com.ibm.casemgmt.api.objectref.FolderReference;
import com.ibm.casemgmt.api.objectref.ObjectStoreReference;
import com.ibm.casemgmt.api.properties.CaseMgmtProperty;
import com.ibm.casemgmt.api.tasks.Task;

import lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient;
import lu.mtn.ibm.casemanager.client.dto.CaseInstance;
import lu.mtn.ibm.casemanager.client.dto.CaseManagerCreationRequest;
import lu.mtn.ibm.casemanager.client.dto.CaseManagerModificationRequest;
import lu.mtn.ibm.casemanager.client.dto.CaseTask;
import lu.mtn.ibm.casemanager.client.ri.util.CaseManagerUtil;
import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentOperationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;
import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.client.ri.FileNetCERIClient;

/**
 * @author MTN
 *
 */
public class FileNetCaseManagerRIClient extends FileNetCERIClient implements FileNetCaseManagerClient {

    /**
     * @param ceWsUrl
     * @param username
     * @param password
     * @param defaultObjectStore
     * @param jaasStanza
     */
    public FileNetCaseManagerRIClient(String ceWsUrl, String username, String password, String defaultObjectStore, String jaasStanza) {
        super(new CaseManagerConnection(ceWsUrl, username, password, jaasStanza), defaultObjectStore);
    }
    
    /**
     * @param ceWsUrl
     * @param username
     * @param password
     * @param defaultObjectStore
     * @param jaasStanza
     */
    public FileNetCaseManagerRIClient(AbstractCaseManagerConnection connection, String defaultObjectStore) {
        super(connection, defaultObjectStore);
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#findCase(java.lang.String)
     */
    @Override
    public CaseInstance findCase(String caseId) throws ServiceCallException {
        return this.findCase(caseId, osName);
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#findCase(java.lang.String, java.lang.String)
     */
    @Override
    public CaseInstance findCase(String caseId, String objectStoreName) throws ServiceCallException {
        return this.findCase(caseId, objectStoreName, false);
    }
    
    @SuppressWarnings("unchecked")
    public CaseInstance findCase(String caseId, String objectStoreName, boolean fetchTasks) throws ServiceCallException {
        
        Case caseObject = this.findCaseObject(caseId, objectStoreName);
        CaseInstance instance = null;
        if (caseObject != null) {
            
            FolderReference caseFolderRef = caseObject.getFolderReference();
            Folder folder = caseFolderRef.fetchOptionalCEObject();
            folder.refresh();
            
            try {
                instance = new CaseInstance();
                instance.setCaseId(folder.getProperties().getStringValue(CM_ACM_CASE_IDENTIFIER));
                instance.setCaseType(folder.getClassName());
                
                for (Iterator<Property> ite = folder.getProperties().iterator(); ite.hasNext();) {

                    Property property = ite.next();
                    instance.getProperties().put(property.getPropertyName(), FileNetCEApiUtil.getPropertyValue(property));
                }
                
                if (fetchTasks) {
                    List<Task> tasks = caseObject.fetchTasks(); 
                    for (Task task : tasks) {
                        CaseTask ct = new CaseTask();
                        ct.setId(task.getId().toString());
                        ct.setName(task.getTaskTypeName());
                        ct.setLaunchMode(task.getLaunchMode().name());
                        ct.setState(task.getState().getValue());

                        for (CaseMgmtProperty prop : task.getProperties().asList()) {
                            ct.getProperties().put(prop.getSymbolicName(), CaseManagerUtil.getPropertyValue(prop));
                        }
                        
                        instance.getTasks().add(ct);
                    }
                }
                DocumentSet set = folder.get_ContainedDocuments();
                for (Iterator<Document> it = set.iterator(); it.hasNext(); ) {
                    Document doc = it.next();
                    
                    CEDocument ceDoc = new CEDocument();
                    ceDoc.setDocumentClass(doc.getClassName());
                    ceDoc.setId(doc.get_Id().toString());

                    for (Iterator<Property> ite = doc.getProperties().iterator(); ite.hasNext();) {

                        Property property = ite.next();
                        ceDoc.getProperties().put(property.getPropertyName(), FileNetCEApiUtil.getPropertyValue(property));
                    }
                    ceDoc.setContents(FileNetCEApiUtil.getContents(doc));
                    
                    instance.getDocuments().add(ceDoc);
                }
            } catch (IOException e) {
                throw new ServiceCallException(e);
            }
        }
        return instance;
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#existCase(java.lang.String)
     */
    @Override
    public boolean existCase(String caseId) throws ServiceCallException {
        return this.existCase(caseId, osName);
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#existCase(java.lang.String, java.lang.String)
     */
    @Override
    public boolean existCase(String caseId, String objectStoreName) throws ServiceCallException {
        return this.findCaseObject(caseId, objectStoreName) != null;
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#startCase(lu.mtn.ibm.casemanager.client.dto.CaseManagerCreationRequest)
     */
    @Override
    public String startCase(CaseManagerCreationRequest request) throws ServiceCallException {
        return this.startCase(request, osName);
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#startCase(lu.mtn.ibm.casemanager.client.dto.CaseManagerCreationRequest, java.lang.String)
     */
    @Override
    public String startCase(CaseManagerCreationRequest request, String objectStoreName) throws ServiceCallException {
        Case newCase = this.createCase(request.getCaseType(), request.getCaseProperties(), request.getDocuments(), objectStoreName);
        
        return (String) newCase.getProperties().get(CM_ACM_CASE_IDENTIFIER).getValue();
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#updateCase(lu.mtn.ibm.casemanager.client.dto.CaseManagerModificationRequest)
     */
    @Override
    public void updateCase(CaseManagerModificationRequest request) throws ServiceCallException {
        this.updateCase(request, osName);
    }

    /**
     * @see lu.mtn.ibm.casemanager.client.FileNetCaseManagerClient#updateCase(lu.mtn.ibm.casemanager.client.dto.CaseManagerModificationRequest, java.lang.String)
     */
    @Override
    public void updateCase(CaseManagerModificationRequest request, String objectStoreName) throws ServiceCallException {
        Case caseFolder = findCaseObject(request.getCaseId(), objectStoreName);
        
        updateCase(caseFolder, request.getCaseProperties(), request.getDocuments(), objectStoreName);
    }

    public Case createCase(String caseType, Map<String, Object> caseProperties, List<DocumentOperationRequest> documents, String objectStoreName) throws ServiceCallException {
        ObjectStore os = this.getObjectStore(objectStoreName);
        
        ObjectStoreReference osRef = new ObjectStoreReference(os); 
        CaseType caseT = CaseType.fetchInstance(osRef, caseType);
        Case newCase = Case.createPendingInstance(caseT);
        
        for(Entry<String, Object> entry : caseProperties.entrySet()){
            newCase.getProperties().putObjectValue(entry.getKey(), entry.getValue());
        }
       
        newCase.save(RefreshMode.REFRESH, null, ModificationIntent.MODIFY);
        Case theCase = newCase;
        int maxTry = 0;
        boolean created = false;
        
        while(++maxTry < 40){
            CaseState currentState = theCase.getState();
            if (currentState.equals(CaseState.WORKING)){
                created = true;
                break;
            }
            try {
                Thread.sleep(250);
                try {
                   theCase = Case.fetchInstance(osRef, newCase.getId(), null, ModificationIntent.MODIFY);
                 } catch (Exception e) {
                           Thread.sleep(250);
                           theCase = Case.fetchInstance(osRef, newCase.getId(), null, ModificationIntent.MODIFY);
                 }
            } catch (InterruptedException e) {
                throw new ServiceCallException(e);
            }
        }
        
        if (!created) {
            throw new IllegalStateException("The case is not in working state after " + maxTry + " tries");
        }
       
        processDocuments(theCase, documents, objectStoreName);
        
        return theCase;
    }

    public void updateCase(Case caseFolder, Map<String, Object> caseProperties, List<DocumentOperationRequest> documents, String objectStoreName) throws ServiceCallException {
        for(Entry<String, Object> entry : caseProperties.entrySet()){
            caseFolder.getProperties().putObjectValue(entry.getKey(), entry.getValue());
        }
        caseFolder.save(RefreshMode.NO_REFRESH, null, ModificationIntent.MODIFY);
        
        processDocuments(caseFolder, documents, objectStoreName);
    }

    protected void processDocuments(Case caseObject, List<DocumentOperationRequest> documents, String objectStoreName) throws ServiceCallException {
        if (documents != null) {
            
            FolderReference caseFolderRef = caseObject.getFolderReference();
            Folder caseFolder = caseFolderRef.fetchOptionalCEObject();
            caseFolder.refresh();
            
            for(DocumentOperationRequest op : documents) {
                 
                if (op instanceof DocumentCreationRequest) {
                     
                    DocumentCreationRequest request = (DocumentCreationRequest) op;
                    request.setFolder(caseFolder.get_PathName());
                     
                    this.createDocument(request, objectStoreName);
                } else {
                    this.updateDocument((DocumentModificationRequest) op, objectStoreName);
                }
            }
            caseFolder.refresh();
        }
    }

    public Case findCaseObject(String caseId, String objectStoreName) throws ServiceCallException {
        
        ObjectStore os = this.getObjectStore(objectStoreName);
        
        ObjectStoreReference osRef = new ObjectStoreReference(os); 
        
        try {
            Case c = Case.fetchInstanceFromIdentifier(osRef, caseId);
            return c;
        } catch (CaseMgmtException e) {
            if (e.hasCategory(ErrorCategory.CASE_NOT_FOUND) || e.hasCategory(ErrorCategory.NOT_FOUND)) {
                return null;
            }
            throw e;
        }
    }
}
