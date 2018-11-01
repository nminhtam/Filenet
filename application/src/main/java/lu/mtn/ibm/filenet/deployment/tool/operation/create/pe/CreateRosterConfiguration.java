/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.pe;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import filenet.vw.api.VWException;
import filenet.vw.api.VWRosterDefinition;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingRosterPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateRosterConfiguration extends AbstractCreatePEConfigurationOperation {

    private String rosterName;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateRosterConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.AbstractCreatePEConfigurationOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.rosterName = rootElement.getAttribute("name");

        prerequisites.add(new ExistingRosterPrerequisite(this.rosterName, false));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {
            VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
            VWRosterDefinition roster = config.createRosterDefinition(rosterName);
            roster.setDescription(description);

            createFields(roster);
            createIndexes(roster);

            // Commit changes
            String[] errors = config.commit();
            if (errors != null) {
                System.out.println("Errors: " + Arrays.toString(errors));
            } else {
                System.out.println("All changes have been committed.");
            }

        } catch (VWException e) {
            throw new OperationExecutionException(e);
        } catch (ParserConfigurationException e) {
            throw new OperationExecutionException(e);
        } catch (SAXException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
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
        return String.format("Create PE Roster \"%s\"", this.rosterName);
    }

}
