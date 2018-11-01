/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.AccessType;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.constants.SecurityPrincipalType;
import com.filenet.api.core.Factory;
import com.filenet.api.security.AccessPermission;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.dto.Permission;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractUpdateSecurityOperation<T> extends AbstractUpdateOperation {

    protected Collection<Permission> permissions;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractUpdateSecurityOperation(String xml) throws OperationInitializationException {
        super(xml);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        T object = findObject(context);
        AccessPermissionList accessPermissionList = getAccessPermissionList(object, context);

        this.updatePermissions(context, accessPermissionList);

        update(object);
        return null;
    }


    protected abstract T findObject(ExecutionContext context);

    protected abstract AccessPermissionList getAccessPermissionList(T object, ExecutionContext context);

    protected abstract void update(T object);


    /**
     * @param node
     */
    protected void processPermissions(Node node) {

        this.permissions = new HashSet<Permission>();
        this.processPermissions(node, this.permissions);
    }

    /**
     * @param accessPermissionList
     */
    protected void updatePermissions(ExecutionContext context, AccessPermissionList accessPermissionList) {
        this.updatePermissions(context, accessPermissionList, this.permissions);
    }


    /**
     * @param node
     * @param perms
     */
    protected void processPermissions(Node node, Collection<Permission> perms) {

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node child = children.item(i);

            if (child.getNodeType() != 3 && "permission".equals(child.getNodeName())) {

                String user = child.getAttributes().getNamedItem("user").getNodeValue();
                String type = child.getAttributes().getNamedItem("type").getNodeValue();
                String accessType = child.getAttributes().getNamedItem("accessType").getNodeValue();
                String accessValue = child.getAttributes().getNamedItem("accessMask").getNodeValue();
                String depth = child.getAttributes().getNamedItem("depth").getNodeValue();

                perms.add(new Permission(user, type, accessType, accessValue, depth));
            }
        }
    }

    /**
     * @param context
     * @param accessPermissionList
     * @param perms
     */
    @SuppressWarnings("unchecked")
    protected void addNewPermissions(ExecutionContext context, AccessPermissionList accessPermissionList, Collection<Permission> perms) {
        if (perms != null) {
        	for (Permission permission : perms) {

            	SecurityPrincipalType userType = getUserType(permission.getType());
            	boolean exist = false;
            	if (SecurityPrincipalType.USER.equals(userType)) {
                	try {
                    	Factory.User.fetchInstance(context.getConnection().getObjectStore().getConnection(), permission.getUser(), null);
                    	exist = true;
                	} catch (Exception e) {
                    	exist = false;
                	}

            	} else if (SecurityPrincipalType.GROUP.equals(userType)) {
                	try {
                    	Factory.Group.fetchInstance(context.getConnection().getObjectStore().getConnection(), permission.getUser(), null);
                    	exist = true;
                	} catch (Exception e) {
                    	exist = false;
                	}
            	}

            	if (exist) {
                	AccessPermission ap = Factory.AccessPermission.createInstance();
                	ap.set_GranteeName(permission.getUser());
                	ap.set_AccessType(getAccessType(permission.getAccessType()));
                	ap.set_AccessMask(getAccessMask(permission.getAccessMask()));
                	ap.set_InheritableDepth(getDepth(permission.getDepth()));

                	accessPermissionList.add(ap);
            	}
        	}
    	}
    }

    /**
     * @param accessPermissionList
     * @param perms
     */
    @SuppressWarnings("unchecked")
    protected void updatePermissions(ExecutionContext context, AccessPermissionList accessPermissionList, Collection<Permission> perms) {

        Map<String, Permission> list = new HashMap<String, Permission>();
        if (perms != null) {
        	for (Permission permission : perms) {
            	list.put(permission.getUser(), permission);
        	}
        }

        Set<AccessPermission> accessToRemove = new HashSet<AccessPermission>();
        // Iterate over all current permissions to update it or to put it in the delete list if they are not defined anymore
        for (Iterator<AccessPermission> it = accessPermissionList.iterator(); it.hasNext();) {
            AccessPermission ap = it.next();

            if (!PermissionSource.SOURCE_PARENT.equals(ap.get_PermissionSource())) {
            	Permission permission = list.remove(ap.get_GranteeName());

            	if (permission != null) {
                	ap.set_AccessType(getAccessType(permission.getAccessType()));
                	ap.set_AccessMask(getAccessMask(permission.getAccessMask()));
                	ap.set_InheritableDepth(getDepth(permission.getDepth()));
            	} else {
                	// Permission not present anymore.
                	accessToRemove.add(ap);
            	}
        	}
        }

        // Delete undefined permissions
        for (AccessPermission permission : accessToRemove) {
            accessPermissionList.remove(permission);
        }

        // Return the list of permissions which doesn't exist before so they nee
        addNewPermissions(context, accessPermissionList, list.values());
    }

    protected AccessType getAccessType(String accessType) {
        return AccessType.getInstanceFromInt(Integer.valueOf(accessType));
    }

    protected Integer getAccessMask(String accessMask) {
        return Integer.valueOf(accessMask);
    }

    protected Integer getDepth(String depth) {
        return Integer.valueOf(depth);
    }

    protected SecurityPrincipalType getUserType(String userType) {
        return SecurityPrincipalType.getInstanceFromInt(Integer.valueOf(userType));
    }

    /**
     * @return the defaultInstancePermissions
     */
    public Collection<Permission> getpermissions() {
        return this.permissions;
    }
}
