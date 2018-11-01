/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.events.EventAction;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateEventAction extends AbstractCreateOperation {

    private String codeModuleId;

    private String programId;

    private String displayName;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateEventAction(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        codeModuleId = rootElement.getAttribute("codeModuleId");
        programId = rootElement.getAttribute("programId");
        displayName = rootElement.getAttribute("displayName");

        if (ExecutionContext.isIdMustBeInterpreted(codeModuleId)) {
            prerequisites.add(new ExistingIdRefPrerequisite(codeModuleId));
        } else {
            prerequisites.add(new ExistingObjectPrerequisite(true, codeModuleId, true, "The codeModule with id " + codeModuleId + " must exist."));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        CodeModule codeModule = Factory.CodeModule.fetchInstance(os, new Id(context.interpretId(codeModuleId)), null);

        EventAction eventAction = Factory.EventAction.createInstance(os, null);
        eventAction.set_ProgId(programId);
        eventAction.set_CodeModule(codeModule);
        eventAction.set_DisplayName(displayName);
        eventAction.save(RefreshMode.REFRESH);

        return eventAction.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create EventAction \"" + this.displayName + "\"";
    }

}
