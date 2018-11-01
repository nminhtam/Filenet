/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.pe;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import filenet.vw.api.VWException;
import filenet.vw.api.VWLogDefinition;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingEvenLogPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateEventLogConfiguration extends AbstractCreatePEConfigurationOperation {

    private String eventLogName;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateEventLogConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.AbstractCreatePEConfigurationOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.eventLogName = rootElement.getAttribute("name");

        prerequisites.add(new ExistingEvenLogPrerequisite(this.eventLogName, false));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {
            VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
            VWLogDefinition eventLog = config.createLogDefinition(eventLogName);
            eventLog.setDescription(description);

            // Commit changes
            String[] errors = config.commit();
            if (errors != null) {
                System.out.println("Errors: " + Arrays.toString(errors));
            } else {
                System.out.println("All changes have been committed.");
            }

        } catch (VWException e) {
            throw new OperationExecutionException(e);
        } catch (Exception e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return String.format("Create PE EventLog \"%s\"", this.eventLogName);
    }

}
