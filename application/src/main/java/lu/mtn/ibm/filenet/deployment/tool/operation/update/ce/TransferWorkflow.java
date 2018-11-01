/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.WorkflowDefinition;
import com.filenet.api.util.Id;

import filenet.vw.api.VWException;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWTransferResult;
import filenet.vw.api.VWWorkflowDefinition;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class TransferWorkflow extends Operation implements ExecutableOperationOnQuery {

    private String id;

    /**
     * @param id
     * @throws OperationInitializationException
     */
    public TransferWorkflow(WorkflowDefinition workflowDefinition) throws OperationInitializationException {
        super();
        this.id = workflowDefinition.get_Id().toString();
    }

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public TransferWorkflow(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        id = rootElement.getAttribute("id");

        if (ExecutionContext.isIdMustBeInterpreted(id)) {
            prerequisites.add(new ExistingIdRefPrerequisite(id));
        } else {
            prerequisites.add(new ExistingObjectPrerequisite(true, id, true, "The workflowDefinition with id " + id + " must exist."));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        // Get the Workflow Definition document object.
        WorkflowDefinition wfdDocument = Factory.WorkflowDefinition.fetchInstance(os, new Id(context.interpretId(id)), null);

        // Get the current VW version number to test its validity.
        String vwCurrentVersion = wfdDocument.get_VWVersion();

        try {
            // Get the VWSession object for testing the VW version number.
            VWSession vwSession = context.getConnection().getVWSession();
            if (vwCurrentVersion == null || !vwSession.checkWorkflowIdentifier(vwCurrentVersion)) {
                String vwNewVersion;
                System.out.println("Workflow definition is not up to date, transfer it.");
                vwNewVersion = transferWorkflowDefinition(os, vwSession, wfdDocument);
                wfdDocument.set_VWVersion(vwNewVersion);
                wfdDocument.save(RefreshMode.REFRESH);
            }
        } catch (VWException e) {
            throw new OperationExecutionException(e);
        }
    }

    public String transferWorkflowDefinition(ObjectStore os, VWSession vwSession, WorkflowDefinition workflowDocCE) throws OperationExecutionException {
        String workflowVersion;
        // In addition to the VWSession object, the following Process Java API objects are used.
        VWWorkflowDefinition workflowDocPE;
        VWTransferResult transferResult;
        String canonicalName;
        try {
            workflowVersion = null;

            workflowDocPE = new VWWorkflowDefinition();
            transferResult = null;

            // Get the definition content from the Content Engine object store to be transferred to Process Engine.
            // Calls getContents method.
            workflowDocPE = VWWorkflowDefinition.read(getContents(os.get_Name(), workflowDocCE));

            // Get the current version series id and use it to create a unique name for the transferred workflow.
            String verSerId = workflowDocCE.get_VersionSeries().get_Id().toString();
            canonicalName = "document" + ":" + os.get_Name() + ":" + verSerId + ":" + workflowDocCE.get_Id().toString();

            try {
                transferResult = vwSession.transfer(workflowDocPE, canonicalName, false, false);
            } catch (VWException e) {
                throw new OperationExecutionException(e);
            }
            if (!transferResult.success()) {
                StringBuffer sb = new StringBuffer();
                String[] errors = transferResult.getErrors();
                for (int i = 0; i < errors.length; ++i) {
                    sb.append(errors[i]).append("\n");
                }

                throw new OperationExecutionException("Failed to transfer workflow definition\n" + sb.toString());
            } else {
                workflowVersion = transferResult.getVersion();
            }

        } catch (VWException e) {
            throw new OperationExecutionException(e);
        } catch (OperationExecutionException e) {
            throw e;
        }

        // Transfer the workflow.
        try {
            transferResult = vwSession.transfer(workflowDocPE, canonicalName, false, false);
        } catch (VWException e) {
            throw new OperationExecutionException(e);
        }
        if (!transferResult.success()) {
            throw new OperationExecutionException("Failed to transfer workflow definition");
        } else {
            workflowVersion = transferResult.getVersion();
        }

        // Return identifier of transferred workflow definition.
        return workflowVersion;
    }

    // Returns the workflow definition content.
    public InputStream getContents(String objectStoreName, WorkflowDefinition workflowDocCE) throws OperationExecutionException {

        ContentTransfer ct;
        ContentElementList contentList = workflowDocCE.get_ContentElements();
        try {
            ct = (ContentTransfer) contentList.get(0);
            return ct.accessContentStream();

        } catch (Exception e) {
            throw new OperationExecutionException("Failed to retrieve document content.");
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Transfer workflow with workflowDefinition id " + this.id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
