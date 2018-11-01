/**
 * 
 */
package lu.mtn.ibm.filenet.client.ri;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;

/**
 * @author MTN
 *
 */
public abstract class AbstractFilenetConnection {

    protected Connection con;
    protected Domain dom;
    protected String domainName;
    protected ObjectStoreSet ost;
    protected Set<String> osnames;
    protected boolean isConnected;
    protected Subject subject;
    protected UserContext oldContext;
    
    public AbstractFilenetConnection() {
        osnames = new HashSet<String>();
        isConnected = false;
    }
    
    public abstract void establishConnection();
    
    /*
     * Returns Domain object.
     */
    public Domain fetchDomain() {
        if (dom == null) {
            dom = Factory.Domain.fetchInstance(con, null, null);
        }

        return dom;
    }

    /*
     * Returns ObjectStoreSet from Domain.
     */
    public ObjectStoreSet getOSSet() {
        ost = dom.get_ObjectStores();
        return ost;
    }

    /*
     * Returns vector containing ObjectStore names from object stores available
     * in ObjectStoreSet.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getOSNames() {
        if (osnames.isEmpty()) {
            Iterator<ObjectStore> it = ost.iterator();
            while (it.hasNext()) {
                ObjectStore os = it.next();
                osnames.add(os.get_DisplayName());
            }
        }
        return osnames;
    }

    /*
     * Checks whether JAAS login has been performed with the Content Engine or
     * not.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /*
     * Returns ObjectStore object for supplied object store name.
     */
    public ObjectStore fetchOS(String name) {
        ObjectStore os = Factory.ObjectStore.fetchInstance(dom, name, null);
        return os;
    }

    /*
     * Returns the domain name.
     */
    public String getDomainName() {
        return domainName;
    }

    public void pushSubject() {
        oldContext = UserContext.get();
        UserContext uc = new UserContext();
        if (uc.getSubject() == null) {
            uc.pushSubject(subject);
        }
        UserContext.set(uc);
    }
    
    public void release() {
        if (oldContext != null)
        {
            UserContext.set(oldContext);
        }
    }
}
