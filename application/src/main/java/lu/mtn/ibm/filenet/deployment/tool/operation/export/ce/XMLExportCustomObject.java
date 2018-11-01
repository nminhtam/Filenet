/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
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
public class XMLExportCustomObject extends AbstractXMLExportOperation<CustomObject> implements ExecutableOperationOnQuery {

    private String id;

    private boolean addFolderPath;


    /**
     * @param xml
     * @param id
     * @param tag
     * @param writerName
     * @param zipContent
     * @param addFolderPath
     * @throws OperationInitializationException
     */
    protected XMLExportCustomObject(String id, String operation, String writerName, boolean addFolderPath) throws OperationInitializationException {
        super(operation, writerName);
        this.id = id;
        this.addFolderPath = addFolderPath;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CustomObject", id), true, "The custom object with id " + id + " must exist."));
    }


    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportCustomObject(String xml) throws OperationInitializationException {
        super(xml);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        id = rootElement.getAttribute("id");
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CustomObject", id), true, "The custom object with id " + id + " must exist."));

        addFolderPath = rootElement.hasAttribute("addFolderPath") ? Boolean.valueOf(rootElement.getAttribute("addFolderPath")) : Constants.DEFAULT_ADD_FOLDER_PATH;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CUSTOM_OBJ_" + id;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected CustomObject findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        return Factory.CustomObject.fetchInstance(os, new Id(id), null);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(CustomObject object) {
        return object.get_Id().toString();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeDetails(CustomObject customObject, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("class", customObject.getClassName());
        root.setAttribute("name", customObject.get_Name());

        if (addFolderPath) {
            Iterator<ReferentialContainmentRelationship> it =  customObject.get_Containers().iterator();
            if (it.hasNext()) {
                root.setAttribute("folder", ((Folder) it.next().get_Tail()).get_PathName());
            }
        }

        List<String> propertyNames = retrievePropertyNames(customObject.get_ClassDescription());

        this.writeProperties(customObject.getProperties(), root, doc, propertyNames);

        this.write(context, doc);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_CUSTOM_OBJECT;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportCustomObject.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export CustomObject with id " + id;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
