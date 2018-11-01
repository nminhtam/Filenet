/**
 * 
 */
package lu.mtn.ibm.casemanager.client.ri;

import java.util.Locale;

import com.filenet.api.util.UserContext;
import com.ibm.casemgmt.api.context.CaseMgmtContext;

import lu.mtn.ibm.filenet.client.ri.AbstractFilenetConnection;

/**
 * @author MTN
 *
 */
public abstract class AbstractCaseManagerConnection extends AbstractFilenetConnection {

    protected Locale originalLocale;
    
    protected CaseMgmtContext origCmctx;
    
    public void pushLocale(Locale locale) {
        UserContext uc = UserContext.get();
        originalLocale = uc.getLocale();
        
        if (originalLocale != locale) {
            uc.setLocale(locale);
        }
    }
    
    public void release() {
        CaseMgmtContext.set(origCmctx);
        
        if (originalLocale != null)
        {
            UserContext uc = UserContext.get();
            uc.setLocale(originalLocale);
        }
        super.release();
    }
}
