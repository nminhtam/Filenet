/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.collection.EngineCollection;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportFolderSecurity extends AbstractXMLExportSecurity<Folder> {

    private String path;

    private boolean exportSubFolders;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportFolderSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    protected XMLExportFolderSecurity(String path, String operation, String writerName, boolean exportSubClasses) throws OperationInitializationException {
        super(operation, writerName);
        this.path = path;
        this.exportSubFolders = exportSubClasses;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_FOLDER_NAME, path), true, "The folder with path " + path + " must exist."));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        path = rootElement.getAttribute("path");
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_FOLDER_NAME, path), true, "The folder with path " + path + " must exist."));

        exportSubFolders = rootElement.hasAttribute("exportSubFolders") ? Boolean.valueOf(rootElement.getAttribute("exportSubFolders")) : Constants.DEFAULT_EXPORT_SUB_FOLDERS;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "FOLDER-SEC_" + path;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected Folder findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_SECURITY_FOLDER_NAME, path));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The folder " + path + " must exist.");
        }
        return (Folder) it.next();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(Folder folder) {
        return folder.get_Id().toString();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeDetails(Folder folder, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("path", folder.get_PathName());

        Element defInstElement = createElement(doc, root, "permissions");
        writePermissions(defInstElement, doc, folder.get_Permissions());

        this.write(context, doc);

        if (exportSubFolders) {
            try {
                for (Iterator<Folder> iter = folder.get_SubFolders().iterator(); iter.hasNext(); ) {

                    Folder classDef = iter.next();
                    XMLExportFolderSecurity exportClass = new XMLExportFolderSecurity(classDef.get_PathName(), operation, writerName, exportSubFolders);
                    exportClass.execute(context);
                }
            } catch (OperationInitializationException e) {
                throw new OperationExecutionException(e);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        return Constants.TAG_UPDATE_SECURITY_FOLDER;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export Folder Security with path " + path;
    }
}
