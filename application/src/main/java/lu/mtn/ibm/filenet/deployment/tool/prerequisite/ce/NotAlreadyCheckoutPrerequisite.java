/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce;

import java.util.Iterator;

import com.filenet.api.collection.EngineCollection;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;


/**
 * @author NguyenT
 *
 */
public class NotAlreadyCheckoutPrerequisite implements Prerequisite {

    private String id;

    /**
    *
    */
   public NotAlreadyCheckoutPrerequisite(String id) {
       this.id = id;
   }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {
        if (ExecutionContext.isIdMustBeInterpreted(id)) {
            id = context.interpretId(id);
        }

        if (id != null && !id.isEmpty()) {
            String query = String.format(Constants.QUERY_ALREADY_CHECKOUT_DOCUMENT_ID, id);
            SearchSQL sql = new SearchSQL(query);
            SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

            EngineCollection ec = ss.fetchRows(sql, 1, null, Boolean.FALSE);
            Iterator<?> it = ec.iterator();

            return it.hasNext();
        }
        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#isBlocking()
     */
    @Override
    public boolean isBlocking() {
        return true;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#getDescription()
     */
    @Override
    public String getDescription() {
        return "The document with id " + id + " must not be in checkout state.";
    }


}
