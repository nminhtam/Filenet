/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.ObjectStore;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateObjectStoreSecurity extends AbstractUpdateSecurityOperation<ObjectStore> {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateObjectStoreSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ObjectStore findObject(ExecutionContext context) {
        return context.getConnection().getObjectStore();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#getAccessPermissionList(java.lang.Object, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected AccessPermissionList getAccessPermissionList(ObjectStore object, ExecutionContext context) {
        return object.get_Permissions();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#update(java.lang.Object)
     */
    @Override
    protected void update(ObjectStore object) {
        object.save(RefreshMode.NO_REFRESH);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
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
        return "Update the ObjectStore security";
    }

}
