/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.collection.EngineCollection;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author MTN
 *
 */
public class StoreIdReference extends Operation {

    private String query;

    private String idRef;


    /**
     * @param xmlOperation
     * @throws OperationInitializationException
     */
    public StoreIdReference(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.query = rootElement.getAttribute("query");
        this.idRef = rootElement.getAttribute("idRef");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {

                SearchSQL sql = new SearchSQL(query);
                SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

                EngineCollection ec = ss.fetchRows(sql, 1, null, Boolean.FALSE);

                Iterator<?> it = ec.iterator();
                if (it.hasNext()) {

                    RepositoryRow row = (RepositoryRow) it.next();
                    context.getIdRefs().put(idRef, row.getProperties().getIdValue("Id").toString());
                }
        } catch (SecurityException e) {
            throw new OperationExecutionException(e);
        } catch (IllegalArgumentException e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "StoreIdReference with ref \"" + this.idRef + "\" from result of query \"" + this.query + "\"";
    }
}
