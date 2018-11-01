/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractOperationDatasModifier extends Operation {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractOperationDatasModifier(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.init(rootElement);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(be.portima.dmpoc.installation.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void execute(ExecutionContext context) throws OperationExecutionException {

        ((Set<AbstractOperationDatasModifier>) context.getVariables().get(Constants.OPERATION_MODIFIERS)).add(this);
    }

    public abstract void init(Element rootElement) throws OperationInitializationException;

    public abstract boolean isApplicable(Operation operation);

    public abstract void modify(Operation operation) throws OperationExecutionException;
}
