/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.pe;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filenet.vw.api.VWAttributeInfo;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportRegisteredLibrariesConfiguration  extends AbstractXMLExportPEConfigurationOperation<VWAttributeInfo> {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportRegisteredLibrariesConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        writerName = rootElement.getAttribute("writer");
        if (writerName == null) {
            throw new IllegalArgumentException("Writer paramater can't be null.");
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "PE_RegisteredLibs";
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected VWAttributeInfo findObject(ExecutionContext context) {
        VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
        return config.getAttributeInfo();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(VWAttributeInfo object) {
        return null;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Element, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(VWAttributeInfo info, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();

        String libs = (String) info.getFieldValue("F_AdditionalLibraryFiles");

        root.appendChild(doc.createCDATASection(libs));
        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        return Constants.TAG_UPDATE_REGISTERED_LIBS_CONFIG;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export configuration for PE Registered libraries";
    }

}
