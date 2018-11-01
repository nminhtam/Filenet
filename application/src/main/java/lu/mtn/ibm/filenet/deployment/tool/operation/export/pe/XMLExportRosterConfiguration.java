/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.pe;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filenet.vw.api.VWRosterDefinition;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingRosterPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportRosterConfiguration extends AbstractXMLExportPEConfigurationOperation<VWRosterDefinition> {

    private String name;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportRosterConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.name = rootElement.getAttribute("name");

        prerequisites.add(new ExistingRosterPrerequisite(name, true));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "ROSTER_" + this.name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected VWRosterDefinition findObject(ExecutionContext context) {
        VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
        VWRosterDefinition roster = config.getRosterDefinition(name);
        return roster;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(VWRosterDefinition object) {
        return this.name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Element, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(VWRosterDefinition roster, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", name);
        if (roster.getDescription() != null && !roster.getDescription().isEmpty()) {
            root.setAttribute("description", roster.getDescription());
        }

        this.writeExposedFields(root, doc, roster.getFields());
        this.writeIndexes(root, doc, roster.getIndexes());

        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_ROSTER_CONFIG;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_ROSTER_CONFIG;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportRosterConfiguration.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export configuration for PE Roster \"" + name + "\"";
    }

}
