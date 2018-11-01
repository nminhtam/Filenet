/**
 * 
 */
package lu.mtn.ibm.filenet.client.ri;

import java.security.AccessController;

import javax.security.auth.Subject;

import com.filenet.api.core.EntireNetwork;
import com.filenet.api.core.Factory;

import filenet.vw.server.Configuration;

/**
 * @author MTN
 *
 */
public class PEConnection extends AbstractFilenetConnection {

    /**
     * @see lu.mtn.ibm.filenet.client.ri.AbstractFilenetConnection#establishConnection()
     */
    @Override
    public void establishConnection() {
        
        subject = Subject.getSubject(AccessController.getContext());
        String ceURI = null;
        
        ceURI = Configuration.GetCEURI(null, null);
        con = Factory.Connection.getConnection(ceURI);

        pushSubject();

        EntireNetwork entireNetwork = Factory.EntireNetwork.fetchInstance(con, null);

        if (entireNetwork == null)
        {
            throw new ExceptionInInitializerError("Cannot log in to " + ceURI);
        }

        dom = entireNetwork.get_LocalDomain();
        
        domainName = dom.get_Name();
        isConnected = true;
    }

}
