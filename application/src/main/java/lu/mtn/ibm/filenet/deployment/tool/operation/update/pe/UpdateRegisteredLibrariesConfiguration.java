/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.pe;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import filenet.vw.api.VWAttributeInfo;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateRegisteredLibrariesConfiguration extends AbstractUpdateOperation {

    private String libs;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateRegisteredLibrariesConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        libs = rootElement.getTextContent();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {
        VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();

        VWAttributeInfo info = config.getAttributeInfo();
        if (!libs.equals(info.getFieldValue("F_AdditionalLibraryFiles)"))) {
            info.setFieldValue("F_AdditionalLibraryFiles", libs);
            config.setAttributeInfo(info);

            String[] errors = config.commit();
            if (errors != null) {
                System.out.println("Errors: " + Arrays.toString(errors));
            } else {
                System.out.println("All changes have been committed.");
            }
        }
        return null;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update configuration for PE Registered libraries";
    }

}
