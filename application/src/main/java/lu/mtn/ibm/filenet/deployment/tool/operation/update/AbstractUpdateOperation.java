/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author nguyent
 *
 */
public abstract class AbstractUpdateOperation extends Operation {

    private String idRef;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractUpdateOperation(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public final void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.initInternal(rootElement, prerequisites);

        if(rootElement.hasAttribute("idRef")) {
            idRef = rootElement.getAttribute("idRef");
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void execute(ExecutionContext context) throws OperationExecutionException {

        for (AbstractOperationDatasModifier mapper : (Set<AbstractOperationDatasModifier>) context.getVariables().get(Constants.OPERATION_MODIFIERS)) {
            if (mapper.isApplicable(this)) {
                mapper.modify(this);
            }
        }
        String id = this.executeInternal(context);

        if (idRef != null) {
            context.getIdRefs().put(idRef, id);
        }
    }

    protected abstract void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException;

    protected abstract String executeInternal(ExecutionContext context) throws OperationExecutionException;
}
