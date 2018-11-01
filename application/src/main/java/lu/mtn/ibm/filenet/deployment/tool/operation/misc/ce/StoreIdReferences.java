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
public class StoreIdReferences extends Operation {

    private String query;

    private String prefix;

    private String propertyName;


    /**
     * @param xmlOperation
     * @throws OperationInitializationException
     */
    public StoreIdReferences(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.query = rootElement.getAttribute("query");
        this.prefix = rootElement.getAttribute("prefix");
        this.propertyName = rootElement.getAttribute("propertyName");

        if(!query.matches("[Ss][Ee][Ll][Ee][Cc][Tt].+DocumentTitle.* [Ff][Rr][Oo][Mm].*")) {
            throw new OperationInitializationException("The propertyName " + propertyName + " is not present in the query");
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {
            SearchSQL sql = new SearchSQL(query);
            SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

            EngineCollection ec = ss.fetchRows(sql, 2000, null, Boolean.TRUE);

            for(Iterator<?> it = ec.iterator(); it.hasNext(); ) {

                RepositoryRow row = (RepositoryRow) it.next();
                context.getIdRefs().put(prefix + row.getProperties().getObjectValue(propertyName), row.getProperties().getIdValue("Id").toString());
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
        return "StoreIdReferences with property \"" + this.propertyName + "\" from result of query \"" + this.query + "\"";
    }
}
