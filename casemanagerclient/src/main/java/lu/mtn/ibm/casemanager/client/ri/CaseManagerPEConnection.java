/**
 * 
 */
package lu.mtn.ibm.casemanager.client.ri;

import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;

import com.filenet.api.core.EntireNetwork;
import com.filenet.api.core.Factory;
import com.ibm.casemgmt.api.context.CaseMgmtContext;
import com.ibm.casemgmt.api.context.SimpleP8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleVWSessionCache;

import filenet.vw.server.Configuration;

/**
 * @author MTN
 *
 */
public class CaseManagerPEConnection extends AbstractCaseManagerConnection {

    /**
     * 
     */
    public CaseManagerPEConnection() {
        super();
    }

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

        // setting up CaseMmgtContext for Case API
        SimpleVWSessionCache vwSessCache = new SimpleVWSessionCache();
        
        if (CaseMgmtContext.isContextSpecified())
            origCmctx = CaseMgmtContext.get();
        pushLocale(Locale.getDefault());

        CaseMgmtContext cmc = new CaseMgmtContext(vwSessCache, new SimpleP8ConnectionCache());
        CaseMgmtContext.set(cmc);
        
        dom = entireNetwork.get_LocalDomain();
        
        domainName = dom.get_Name();
        isConnected = true;        
    }
}
