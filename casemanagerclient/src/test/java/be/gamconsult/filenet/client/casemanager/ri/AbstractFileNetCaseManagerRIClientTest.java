/**
 *
 */
package be.gamconsult.filenet.client.casemanager.ri;

import org.junit.Before;

import com.filenet.api.core.ObjectStore;

import lu.mtn.ibm.casemanager.client.ri.FileNetCaseManagerRIClient;

/**
 * @author MTN
 *
 */
public class AbstractFileNetCaseManagerRIClientTest {

    private static final String OBJECT_STORE = "TAROS";

    protected FileNetCaseManagerRIClient client;

    protected ObjectStore os;

    @Before
    public void init() {
//      System.setProperty("javax.net.debug", "all");
//      System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//      System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//      System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//      System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

//        System.setProperty("http.proxyHost", "localhost");
//        System.setProperty("http.proxyPort", "8008");

        //this.client = new FileNetCERIClient("http://192.168.64.3:9080/wsi/FNCEWS40MTOM", "Fnet_admin", "Filenet01", "OS1", null);
        this.client = new FileNetCaseManagerRIClient("http://192.168.116.137:9080/wsi/FNCEWS40MTOM", "ldap_admin", "Filenet01", "TAROS", "FileNetP8WSI");

        os = client.getObjectStore(OBJECT_STORE);
    }
}
