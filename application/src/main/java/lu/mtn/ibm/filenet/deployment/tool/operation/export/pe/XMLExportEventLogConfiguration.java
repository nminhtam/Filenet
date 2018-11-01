/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.pe;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filenet.vw.api.VWLogDefinition;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingEvenLogPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportEventLogConfiguration extends AbstractXMLExportPEConfigurationOperation<VWLogDefinition> {

    private String name;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportEventLogConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.name = rootElement.getAttribute("name");

        prerequisites.add(new ExistingEvenLogPrerequisite(name, true));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "EVENTLOG_" + this.name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected VWLogDefinition findObject(ExecutionContext context) {
        VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
        VWLogDefinition eventLog = config.getLogDefinition(name);
        return eventLog;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(VWLogDefinition object) {
        return name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(VWLogDefinition eventLog, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", name);
        if (eventLog.getDescription() != null && !eventLog.getDescription().isEmpty()) {
            root.setAttribute("description", eventLog.getDescription());
        }
        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_EVENT_LOG_CONFIG;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_EVENT_LOG_CONFIG;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportEventLogConfiguration.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export configuration for PE EventLog \"" + name + "\"";
    }

}
