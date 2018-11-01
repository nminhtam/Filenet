/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.core.Factory;
import com.filenet.api.events.EventAction;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportEventAction extends AbstractXMLExportOperation<EventAction> implements ExecutableOperationOnQuery {

    private String id;

    private boolean exportCodeModule;

    /**
     * @param xml
     * @param id
     * @param exportCodeModule
     * @throws OperationInitializationException
     */
    public XMLExportEventAction(String id, String operation, String writerName, boolean exportCodeModule) throws OperationInitializationException {
        super(operation, writerName);
        this.id = id;
        this.exportCodeModule = exportCodeModule;
    }

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportEventAction(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        id = rootElement.getAttribute("id");
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CODE_MODULE_EVENT_ID, id), true, "The CodeModuleEvent with id " + id + " must exist."));

        exportCodeModule = rootElement.hasAttribute("exportCodeModule") ? Boolean.valueOf(rootElement.getAttribute("exportCodeModule")) : Constants.DEFAULT_EXPORT_CODE_MODULE;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "EVENT_ACTION_" + id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected EventAction findObject(ExecutionContext context) {
        return Factory.EventAction.fetchInstance(context.getConnection().getObjectStore(), new Id(id), null);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(EventAction event) {
        return event.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Element, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(EventAction event, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        CodeModule cm = event.get_CodeModule();
        if (cm != null) {
            if (exportCodeModule) {
                String codeModuleName = cm.getProperties().getStringValue("documentTitle");
                root.setAttribute("codeModuleId", "#{CM_" + codeModuleName + "}");
                try {
                    XMLExportDocument exportcodeModule = new XMLExportDocument(cm.get_Id().toString(), this.operation, writerName, Constants.DEFAULT_ZIP_CONTENT, Constants.DEFAULT_ADD_FOLDER_PATH);
                    exportcodeModule.setIdRef("CM_" + codeModuleName);
                    exportcodeModule.execute(context);
                } catch (OperationInitializationException e) {
                    throw new OperationExecutionException(e);
                }
            } else {
                root.setAttribute("codeModuleId", cm.get_Id().toString());
            }
        }
        root.setAttribute("displayName", event.get_DisplayName());
        root.setAttribute("programId", event.get_ProgId());

        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_EVENT_ACTION;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportEventAction.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export EventAction with id " + id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
