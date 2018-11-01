/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.core.ObjectStore;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class XmlExportObjectStoreSecurity extends AbstractXMLExportSecurity<ObjectStore> {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XmlExportObjectStoreSecurity(String xml) throws OperationInitializationException {
        super(xml);
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
        return "OBJECT_STORE_SECURITY";
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ObjectStore findObject(ExecutionContext context) {
        return context.getConnection().getObjectStore();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(ObjectStore object) {
        return object.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(ObjectStore object, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();

        Element permissionsElement = createElement(doc, root, "permissions");
        writePermissions(permissionsElement, doc, object.get_Permissions());

        this.write(context, doc);

    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        return Constants.TAG_UPDATE_SECURITY_OBJECT_STORE;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export ObjectStore Security";
    }

}
