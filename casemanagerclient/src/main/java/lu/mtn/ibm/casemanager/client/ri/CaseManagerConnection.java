/**
 * 
 */
package lu.mtn.ibm.casemanager.client.ri;

import java.util.Locale;

import com.filenet.api.util.UserContext;
import com.ibm.casemgmt.api.context.CaseMgmtContext;
import com.ibm.casemgmt.api.context.P8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleP8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleVWSessionCache;

/**
 * @author MTN
 *
 */
public class CaseManagerConnection extends AbstractCaseManagerConnection {

    protected String ceWsUrl;

    protected String username;

    protected String password;

    protected String jaasStanza;
    
    
    /**
     * @param ceWsUrl
     * @param username
     * @param password
     * @param jaasStanza
     */
    public CaseManagerConnection(String ceWsUrl, String username, String password, String jaasStanza) {
        this.ceWsUrl = ceWsUrl;
        this.username = username;
        this.password = password;
        this.jaasStanza = jaasStanza;
    }

    /**
     * @see lu.mtn.ibm.filenet.client.ri.CEConnection#establishConnection()
     */
    @Override
    public void establishConnection() {
        P8ConnectionCache connCache = new SimpleP8ConnectionCache();
        con = connCache.getP8Connection(ceWsUrl);
        subject = UserContext.createSubject(con, username, password, jaasStanza);
        
        if (CaseMgmtContext.isContextSpecified())
            origCmctx = CaseMgmtContext.get();
        
        pushSubject();
        pushLocale(Locale.getDefault());
        
        
        CaseMgmtContext.set(new CaseMgmtContext(new SimpleVWSessionCache(), connCache ));
        
        dom = fetchDomain();
        domainName = dom.get_Name();
        // ost = getOSSet();
        isConnected = true;
    }
}
