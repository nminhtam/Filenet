/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Folder;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateFolderSecurity extends AbstractUpdateSecurityOperation<Folder> {

    private String path;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateFolderSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        path = rootElement.getAttribute("path");

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_FOLDER_NAME, path), true, "The folder " + path + " must exist."));

        NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3 && "permissions".equals(node.getNodeName())) {

                this.processPermissions(node);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update the folder security for " + path;
    }



    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected Folder findObject(ExecutionContext context) {
        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_SECURITY_FOLDER_NAME, path));
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The folder " + path + " must exist.");
        }

        Folder folder = (Folder) it.next();
        return folder;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#getAccessPermissionList(java.lang.Object, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected AccessPermissionList getAccessPermissionList(Folder folder, ExecutionContext context) {
        return folder.get_Permissions();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#update(java.lang.Object)
     */
    @Override
    protected void update(Folder folder) {
        folder.save(RefreshMode.NO_REFRESH);
    }
}
