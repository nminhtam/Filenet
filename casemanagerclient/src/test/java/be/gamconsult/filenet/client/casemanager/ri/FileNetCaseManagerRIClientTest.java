/**
 *
 */
package be.gamconsult.filenet.client.casemanager.ri;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.ibm.casemgmt.api.Case;
import com.ibm.casemgmt.api.objectref.FolderReference;
import com.ibm.casemgmt.api.properties.CaseMgmtProperty;
import com.ibm.casemgmt.api.tasks.LaunchStep;
import com.ibm.casemgmt.api.tasks.Task;

import lu.mtn.ibm.casemanager.client.ri.util.CaseManagerUtil;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentOperationRequest;

/**
 * @author NguyenT
 *
 */
public class FileNetCaseManagerRIClientTest extends AbstractFileNetCaseManagerRIClientTest {

    /**
     *
     */
    @Test
    public void test() throws Exception {

        /*
        CEDocument doc = client.getDocument("{DF4D8F9C-B65C-48B0-8DC7-86B7C9BED676}", new String[] { CEDocument.PROP_CONTENT_ELEMENTS } );
        assertNotNull(doc);
        assertTrue(doc.isContentsInitialized());
*/
/*
        CaseManagerCreationRequest request = new CaseManagerCreationRequest();
        request.setDocumentClassName("Document");
        request.setDocumentName("document.pdf");
        request.setFolder("/test");

        CEDocumentContent content = doc.getContents().get(0);
        request.addContent(content.getName(), content.getContent());

        String id = client.createDocument(request, "OS1");
        assertNotNull(id);
        System.out.println(id);
  */
        
        DocumentCreationRequest request = new DocumentCreationRequest("IcnExternalDocument");
        request.setDocumentName("Case Manager.pdf");
        
        request.getDocProps().put("IcnExternalRepositoryInfo", "OS1|{5989903F-F477-4FC3-BBF8-9769B749DD31}"); // ObjectStore + "|" + DomainId
        request.getDocProps().put("IcnExternalRepositoryType", 3); // IcnRepositoryTypeChoiceList (p8 = 3)
        request.getDocProps().put("IcnExternalDocumentId", "{F4601C8D-E2C1-4CD2-AC58-F10B189DFE24}");
        
        Map<String, Object> caseData = new HashMap<String, Object>();
        List<DocumentOperationRequest> docs = new ArrayList<DocumentOperationRequest>();
        docs.add(request);
        
        client.createCase("AXASI_Indexation", caseData, docs, "TAROS");
        
        //Factory.VersionSeries.fetchInstance(os, null, null)
        /*
        CEDocument doc = client.getCurrentDocumentByVersionSeriesId("{02904C10-1132-44DA-80DB-32AC1D62F162}", "OS1", new String[] {"DocumentTitle", "VersionSeries" }, false);
        System.out.println(doc.get("VersionSeries"));
        */
                        
    }

    /**
    *
    */
    @Test
    public void testSearch() throws Exception {

        Iterator<?> iter = client.search("select This from Document");
        assertNotNull(iter);
        assertTrue(iter.hasNext());

        Object o = iter.next();
        assertNotNull(o);
        assertTrue(o instanceof Document);

        Document value = (Document) o;

        System.out.println(value);

        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            ++count;
        }
        System.out.println(count);
    }
    
    @Test
    public void testFetchTasks() throws Exception {
        
        Case caseObject = client.findCaseObject("AXASI_Sinistre_000000100003", "TAROS");
        
        FolderReference caseFolderRef = caseObject.getFolderReference();
        Folder folder = caseFolderRef.fetchOptionalCEObject();
        folder.refresh();
        
        List<Task> tasks = caseObject.fetchTasks(); 
        for (Task task : tasks) {
            System.out.println(task.getTaskTypeName());
            System.out.println(task.getLaunchMode());
            System.out.println(task.getState());
            
            for (CaseMgmtProperty prop : task.getProperties().asList()) {
                System.out.println(prop.getSymbolicName() + " = " + CaseManagerUtil.getPropertyValue(prop));
            }
            
            if ("Nouvelle Affaire Ajout Doc".equals(task.getName())) {
                
                System.out.println(task.getState());
                
            } else if ("Affaire Existante Ajout Doc".equals(task.getName())) {
                
                System.out.println(task.getState());
            } else if ("AXASI_TEST".equals(task.getTaskTypeName())) {
                
                LaunchStep l = task.initializeNewLaunchStep();
                System.out.println(l);
                System.out.println(l.getParameterNames());
                //System.out.println(t.);
            }
        }
    }
}
