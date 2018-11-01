/**
 *
 */
package be.gamconsult.filenet.client.contentengine.ri;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;


import com.filenet.api.collection.VersionableSet;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;
import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;

/**
 * @author MTN
 *
 */
public class FileNetClientImplDocumentCRUDIntegrationTest extends AbstractFileNetCERIClientTest {


    @Test
    public void testFindInexistingDocument() {

        try {
            client.getDocument("{24F8380F-07DE-4641-BE7E-2BB11E1DAD80}");

            fail("The document must not exist.");

        } catch (ServiceCallException e) {
            assertEquals(ExceptionCode.E_OBJECT_NOT_FOUND, ((EngineRuntimeException) e.getCause()).getExceptionCode());
        }
    }


    @Test
    public void testCreateDocumentWithNoContent() throws ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("testCreateDocumentWithNoContent.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);
        assertTrue(id.matches("\\{[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}\\}"));

        // Verifications
        CEDocument doc = client.getDocument(id);
        assertNotNull(doc);

        assertEquals("testCreateDocumentWithNoContent.test", doc.getProperties().get("Name"));
        assertEquals("Some description", doc.getProperties().get("Description"));

        List<CEDocumentContent> contentElementList = doc.getContents();
        assertNotNull(contentElementList);
        assertTrue(contentElementList.isEmpty());
    }


    @Test
    public void testCreateDocumentWithOneContent() throws IOException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        File file = new File(this.getClass().getResource("/doc2.doc").getFile());

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("testCreateDocumentWithOneContent.test");
        request.setDocProps(props);
        request.addContent(file);

        String id = client.createDocument(request);

        assertNotNull(id);
        assertTrue(id.matches("\\{[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}\\}"));

        // Verifications
        CEDocument doc = client.getDocument(id);
        assertNotNull(doc);

        assertEquals("testCreateDocumentWithNoContent.test", doc.getProperties().get("Name"));
        assertEquals("Some description", doc.getProperties().get("Description"));

        List<CEDocumentContent> contentElementList = doc.getContents();
        assertNotNull(contentElementList);
        assertEquals(1, contentElementList.size());

        ContentTransfer contentTransfer = (ContentTransfer) contentElementList.get(0);
        assertNotNull(contentTransfer);
        assertEquals("doc2.doc", contentTransfer.get_RetrievalName());
        assertEquals("application/msword", contentTransfer.get_ContentType());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(file), bos);
        bos.close();

        byte[] originalContent = bos.toByteArray();

        bos.reset();
        IOUtils.copy(contentTransfer.accessContentStream(), bos);
        bos.close();

        byte[] fileNetContent = bos.toByteArray();

        assertEquals(originalContent.length, fileNetContent.length);
        assertArrayEquals(originalContent, fileNetContent);
    }


    @Test
    public void testCreateDocumentWithTwoContents() throws IOException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        File file1 = new File(this.getClass().getResource("/doc2.doc").getFile());
        File file2 = new File(this.getClass().getResource("/DI-NOVIE.pdf").getFile());

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);
        request.addContent(file1);
        request.addContent(file2);

        String id = client.createDocument(request);

        assertNotNull(id);

        // Verifications
        CEDocument doc = client.getDocument(id);
        assertNotNull(doc);

        List<CEDocumentContent> contentElementList = doc.getContents();
        assertNotNull(contentElementList);
        assertEquals(2, contentElementList.size());
    }


    @Test
    public void testDeleteDocument() throws FileNotFoundException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);

        client.deleteDocument(id);

        try {
            client.getDocument(id);

            fail("The document must not exist anymore.");

        } catch (EngineRuntimeException e) {
            assertEquals(ExceptionCode.E_OBJECT_NOT_FOUND, e.getExceptionCode());
        }
    }

    @Test
    public void testDeleteDocumentContentWrongWay() throws IOException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);
        request.addContent(new File(this.getClass().getResource("/doc2.doc").getFile()));

        String id = client.createDocument(request);

        assertNotNull(id);

        Document doc = Factory.Document.fetchInstance(os, new Id(id), null);
        assertNotNull(doc);

        try {
            doc.set_ContentElements(FileNetCEApiUtil.createContentElementList(null));

            fail("An exception is expected. A checkout is mandatory.");
        } catch (EngineRuntimeException e) {
            assertEquals(ExceptionCode.E_READ_ONLY, e.getExceptionCode());
        }
    }


    @Test
    public void testUpdateDocumentProperties() throws FileNotFoundException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);


        DocumentModificationRequest modRequest = new DocumentModificationRequest(id);
        // Change property.
        modRequest.getDocProps().put("Description", "Another description");

        String updateId = client.updateDocument(modRequest);
        assertNotNull(updateId);
        assertEquals("The id must no change because only properties are modified.", id, updateId);


        // Verifications
        CEDocument doc = client.getDocument(id);
        assertNotNull(doc);

        assertEquals("Another description", doc.get("Description"));
        assertNotSame(doc.get("DateCreated"), doc.get("DateLastModified"));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testAddDocumentContentWrongWay() throws FileNotFoundException, ServiceCallException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);

        Document doc = Factory.Document.fetchInstance(os, id, null);

        // Add a content
        File file = new File(this.getClass().getResource("/doc2.doc").getFile());
        try {
            doc.get_ContentElements().add(FileNetCEApiUtil.createContentTransfer(file.getName(), new FileInputStream(file), null));

            fail("An exception is expected. A checkout is mandatory.");
        } catch (EngineRuntimeException e) {
            assertEquals(ExceptionCode.E_READ_ONLY, e.getExceptionCode());
        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testAddDocumentContentWithVersioning() throws ServiceCallException, IOException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);

        DocumentModificationRequest modRequest = new DocumentModificationRequest(id);
        modRequest.getDocProps().put("Description", "Another description");
        // Add contents
        modRequest.addContent(new File(this.getClass().getResource("/doc2.doc").getFile()));
        modRequest.addContent(new File(this.getClass().getResource("/DI-NOVIE.pdf").getFile()));

        String updatedId = client.updateDocument(modRequest, true, false);
        assertNotNull(updatedId);

        assertFalse("Ids must not be the same because a version is created.", id.equals(updatedId));

        System.out.println(id + " " + updatedId);

        // Verify the two versions exist.
        CEDocument oldVersion = client.getDocument(id.toString());
        assertNotNull(oldVersion);
        assertFalse((Boolean) oldVersion.get("CurrentVersion"));
        assertEquals(1, ((Integer) oldVersion.get("MajorVersionNumber")).intValue());

        CEDocument currentVersion = client.getDocument(updatedId);
        assertNotNull(currentVersion);
        assertTrue((Boolean) oldVersion.get("CurrentVersion"));
        assertEquals(2, ((Integer) oldVersion.get("MajorVersionNumber")).intValue());


        // Another way to check.
        oldVersion = client.getDocument(id);
        VersionableSet versions = (VersionableSet) oldVersion.get("Versions");
        assertNotNull(versions);

        int size = 0;
        for(Iterator<Document> it = versions.iterator(); it.hasNext(); ) {
            ++size;
            Document d = it.next();
            if (id.equals(d.get_Id())) {
                assertFalse(d.get_IsCurrentVersion());
            } else {
                assertEquals(updatedId, d.get_Id());
                assertTrue(d.get_IsCurrentVersion());
            }
        }
        assertEquals(2, size);
    }

    @Test
    public void testAddDocumentContentWithVersioningAndRemoveOtherVersion() throws ServiceCallException, IOException {

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("Description", "Some description");

        DocumentCreationRequest request = new DocumentCreationRequest("CompoundDocumentTest");
        request.setDocumentName("Document.test");
        request.setDocProps(props);

        String id = client.createDocument(request);

        assertNotNull(id);


        DocumentModificationRequest modRequest = new DocumentModificationRequest(id);
        modRequest.getDocProps().put("Description", "Another description");
        // Add a content
        File file = new File(this.getClass().getResource("/doc2.doc").getFile());
        modRequest.addContent(file);

        String updatedId = client.updateDocument(modRequest, false, false);
        assertNotNull(updatedId);

        assertFalse("Ids must not be the same because a version is created.", id.equals(updatedId));

        System.out.println(id + " " + updatedId);

        // Verify only the new version exist.
        try {
            client.getDocument(id);

            fail("The old version of the document must not exist.");

        } catch (ServiceCallException e) {
            assertEquals(ExceptionCode.E_OBJECT_NOT_FOUND, ((EngineRuntimeException) e.getCause()).getExceptionCode());
        }

        Document currentVersion = Factory.Document.fetchInstance(os, updatedId, null);
        assertNotNull(currentVersion);
        assertTrue(currentVersion.get_IsCurrentVersion());
        assertEquals(2, currentVersion.get_MajorVersionNumber().intValue());
    }
}
