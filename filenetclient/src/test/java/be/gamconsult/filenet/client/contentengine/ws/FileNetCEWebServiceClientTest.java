/**
 *
 */
package be.gamconsult.filenet.client.contentengine.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;


import com.filenet.ns.fnce._2006._11.ws.schema.ObjectValue;

import lu.mtn.ibm.filenet.client.FileNetCEClient;
import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;
import lu.mtn.ibm.filenet.client.ws.FileNetCEWebServiceClient;

/**
 * @author NguyenT
 *
 */
public class FileNetCEWebServiceClientTest {

    FileNetCEClient client;

    @Before
    public void setUp() {
        // System.setProperty("javax.net.debug", "all");
        // System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        // System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        // System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        // System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

        client = new FileNetCEWebServiceClient("http://192.168.25.33:9080/wsi/FNCEWS40MTOM", "p8admin", "filenet", "PortimaArchives");
        // client = new FileNetCEWebServiceClient("http://192.168.64.3:9080/wsi/FNCEWS40MTOM", "Fnet_admin", "Filenet01", "OS1");
    }

    /**
     *
     */
    @Test
    public void test() throws Exception {

        CEDocument doc = client.getDocument("{F6D0753C-C264-41CE-BF5A-6E28690BC529}", new String[] { CEDocument.PROP_CONTENT_ELEMENTS });
        assertNotNull(doc);
        assertTrue(doc.isContentsInitialized());

        /*
         * DocumentCreationRequest request = new DocumentCreationRequest(); request.setDocumentClassName("Document");
         * request.setDocumentName("document.pdf"); request.setFolder("/test");
         *
         * CEDocumentContent content = doc.getContents().get(0); request.addContent(content.getName(), content.getContent());
         *
         * String id = client.createDocument(request, "OS1"); assertNotNull(id); System.out.println(id);
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
        assertTrue(o instanceof ObjectValue);

        ObjectValue value = (ObjectValue) o;

        System.out.println(value.getClassId());

        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            ++count;
        }
        System.out.println(count);
    }

    @Test
    public void fillFileNet() throws Exception {

        File dir = new File("c:\\Development\\prj\\newFileDoc\\");

        fillDir(dir);
    }

    protected void fillDir(File dir) throws IOException, ServiceCallException {

        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                fillDir(files[i]);
            } else {
                try {
                    DocumentCreationRequest request = new DocumentCreationRequest("Document");
                    request.setDocumentName(files[i].getName());
                    request.setFolder("/test/subFolder2");
                    request.addContent(files[i]);

                    client.createDocument(request, "OS1");
                } catch (Exception e) {
                    System.out.println(files[i].getAbsolutePath() + " " + files[i].getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
