/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool;


import com.filenet.api.core.ObjectStore;

import filenet.vw.api.VWException;
import filenet.vw.api.VWSession;
import lu.mtn.ibm.filenet.client.ri.FileNetCERIClient;

/**
 * @author NguyenT
 *
 */
public class Connection {

    private FileNetCERIClient client;

    private String url;
    private String user;
    private String password;
    private String objectStore;
    private String connectionPoint;

    // private String jaasStanza;

    /**
     *
     */
    public Connection(String url, String user, String password, String objectStore, String jaasStanza, String connectionPoint) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.objectStore = objectStore;
        this.connectionPoint = connectionPoint;
        client = new FileNetCERIClient(url, user, password, objectStore, jaasStanza);
    }

    /**
     * @return the client
     */
    public FileNetCERIClient getClient() {
        return this.client;
    }

    /**
     * @return
     */
    public ObjectStore getObjectStore() {
        return client.getObjectStore(objectStore);
    }

    /**
     * @return
     */
    public ObjectStore getObjectStore(String objectStoreName) {
        return client.getObjectStore(objectStoreName);
    }

    /**
     * @return
     */
    public VWSession getVWSession() {
        return getVWSession(this.connectionPoint, this.url);
    }

    /**
     * @return
     */
    public VWSession getVWSession(String connectionPoint) {
        return getVWSession(connectionPoint, this.url);
    }

    public VWSession getVWSession(String conn_point, String ceUri) {
        //VWSession vwSession = new VWSession(ceUri);
        //vwSession.setBootstrapCEURI(ceUri);
        try {
            VWSession vwSession = new VWSession();
            vwSession.setBootstrapCEURI(ceUri);
            vwSession.logon(this.user, this.password, conn_point);
            return vwSession;
        } catch (VWException vwe) {
            throw new IllegalStateException("Cannot connect to the connection point.", vwe);
        }
        //return vwSession;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @return the connectionPoint
     */
    public String getConnectionPoint() {
        return this.connectionPoint;
    }
}
