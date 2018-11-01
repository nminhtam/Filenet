/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.dto.Permission;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateClassSecurity extends AbstractUpdateSecurityOperation<ClassDefinition> {

    protected Collection<Permission> defaultInstancePermissions;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateClassSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    private String name;

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        name = rootElement.getAttribute("name");

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class " + name + " must exist."));

        NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {
                if ("permissions".equals(node.getNodeName())) {
                this.processPermissions(node);
                } else if ("defaultInstancePermissions".equals(node.getNodeName())) {
                    this.defaultInstancePermissions = new HashSet<Permission>();
                    this.processPermissions(node, this.defaultInstancePermissions);
                }
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(be.portima.dmpoc.installation.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {
        ClassDefinition object = findObject(context);

        AccessPermissionList accessPermissionList = getAccessPermissionList(object, context);
        this.updatePermissions(context, accessPermissionList, this.permissions);

        accessPermissionList = object.get_DefaultInstancePermissions();
        this.updatePermissions(context, accessPermissionList, this.defaultInstancePermissions);

        update(object);
        return null;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#findObject(be.portima.dmpoc.installation.execution.ExecutionContext)
     */
    @Override
    protected ClassDefinition findObject(ExecutionContext context) {
        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_SECURITY_CLASS_NAME, name));
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The class " + name + " must exist.");
        }

        ClassDefinition classDefinition = (ClassDefinition) it.next();
        return classDefinition;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#getAccessPermissionList(java.lang.Object, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected AccessPermissionList getAccessPermissionList(ClassDefinition classDefinition, ExecutionContext context) {
        return classDefinition.get_Permissions();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.AbstractUpdateSecurityOperation#update(java.lang.Object)
     */
    @Override
    protected void update(ClassDefinition classDefinition) {
        classDefinition.save(RefreshMode.NO_REFRESH);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update the class security for " + name;
    }

    /**
     * @return the defaultInstancePermissions
     */
    public Collection<Permission> getDefaultInstancePermissions() {
        return this.defaultInstancePermissions;
    }
}
