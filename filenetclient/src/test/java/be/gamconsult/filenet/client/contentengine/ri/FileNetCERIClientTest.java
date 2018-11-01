/**
 *
 */
package be.gamconsult.filenet.client.contentengine.ri;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.filenet.api.admin.PEConnectionPoint;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.core.Document;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

/**
 * @author NguyenT
 *
 */
public class FileNetCERIClientTest extends AbstractFileNetCERIClientTest {

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
        DocumentCreationRequest request = new DocumentCreationRequest();
        request.setDocumentClassName("Document");
        request.setDocumentName("document.pdf");
        request.setFolder("/test");

        CEDocumentContent content = doc.getContents().get(0);
        request.addContent(content.getName(), content.getContent());

        String id = client.createDocument(request, "OS1");
        assertNotNull(id);
        System.out.println(id);
  */
        System.out.println(((PEConnectionPoint) os.get_Domain().get_PEConnectionPoints().iterator().next()).get_Name());

        String sqlStr = "Select d.This, d.ContentElements from Document AS d WHERE d.Id = {67AE1029-AB7B-40A1-B836-B6F4526308A3}";

        SearchSQL sql = new SearchSQL(sqlStr);
        SearchScope ss = new SearchScope(os);
        EngineCollection ec = ss.fetchObjects(sql, null, null, Boolean.TRUE);

        Iterator<Object> iter = ec.iterator();
        List<String> ids = new ArrayList<String>();
        while (iter.hasNext()) {
           Document o = (Document) iter.next();
           o.accessContentStream(0);
           System.out.println(o.get_Id());
        }
    }

    /**
    *
    */
    @Test
    public void testSearch() throws Exception {

        Iterator<?> iter = client.search("SELECT [This] FROM [Document] WHERE This INFOLDER '/Saved Searches'");
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
}
