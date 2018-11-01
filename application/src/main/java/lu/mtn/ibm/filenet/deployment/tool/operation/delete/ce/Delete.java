/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.delete.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class Delete extends Operation implements ExecutableOperationOnQuery {

    private String id;

    private String objectClass;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public Delete(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        id = rootElement.getAttribute("id");
        objectClass = rootElement.getAttribute("class");

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, objectClass, id), true, id));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXIST_OBJECT_ID, objectClass, id));
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The object instance of " + objectClass + " with id " + id + " must exist.");
        }

        IndependentlyPersistableObject o = (IndependentlyPersistableObject) it.next();
        o.delete();
        o.save(RefreshMode.NO_REFRESH);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Delete the object of class " + objectClass + " with id " + id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
