/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce;

import java.util.Iterator;


import com.filenet.api.collection.EngineCollection;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingObjectPrerequisite implements Prerequisite {

    private String query;

    private boolean blocking;

    private String description;

    private boolean mustExist;

    /**
     *
     */
    public ExistingObjectPrerequisite(boolean mustExist, String query, boolean blocking, String description) {
        this.query = query;
        this.blocking = blocking;
        this.description = description;
        this.mustExist = mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {

        SearchSQL sql = new SearchSQL(query);
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

        EngineCollection ec = ss.fetchRows(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        return it.hasNext() == mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#isBlocking()
     */
    @Override
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }
}
