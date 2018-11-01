/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security;

import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.constants.PermissionSource;
import com.filenet.api.security.AccessPermission;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator.AccessPermissionComparator;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.modifier.AbstractXMLExportModifier;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 * @param <T>
 *
 */
public abstract class AbstractXMLExportSecurity<T> extends AbstractXMLExportOperation<T> {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractXMLExportSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @param operation
     * @param writerName
     * @throws OperationInitializationException
     */
    public AbstractXMLExportSecurity(String operation, String writerName) throws OperationInitializationException {
        super(operation, writerName);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.modifiers = new HashSet<AbstractXMLExportModifier>();
        writerName = rootElement.getAttribute("writer");
        if (writerName == null) {
            throw new IllegalArgumentException("Writer paramater can't be null.");
        }

        this.initModifiers(rootElement);

        this.initInternal(rootElement, prerequisites);
    }

    @SuppressWarnings("unchecked")
    protected void writePermissions(Element permissionsElement, Document doc, AccessPermissionList accessPermissionList) {

        List<AccessPermission> sortedList = sort(accessPermissionList.iterator(), new AccessPermissionComparator());
        for (AccessPermission ap : sortedList) {

//            System.out.println(ap.get_GranteeName() + " " + ap.get_AccessType() + " " + ap.get_AccessMask() + " " + ap.get_GranteeType() + " "
//                            + ap.get_PermissionSource().toString());
            if (!PermissionSource.SOURCE_PARENT.equals(ap.get_PermissionSource())) {
                Element permissionElement = createElement(doc, permissionsElement, "permission");
                permissionElement.setAttribute("user", ap.get_GranteeName());
                permissionElement.setAttribute("type", String.valueOf(ap.get_GranteeType().getValue()));
                permissionElement.setAttribute("accessType", String.valueOf(ap.get_AccessType().getValue()));
                permissionElement.setAttribute("accessMask", String.valueOf(ap.get_AccessMask()));
                permissionElement.setAttribute("depth", String.valueOf(ap.get_InheritableDepth()));
            }
//            try {
//                if (SecurityPrincipalType.USER.equals(ap.get_GranteeType())) {
//                    User user = findUser(context, ap.get_GranteeName());
//                    System.out.println(user.get_DistinguishedName() + " " + user.get_Email());
//                } else if (SecurityPrincipalType.GROUP.equals(ap.get_GranteeType())) {
//                    Group group = Factory.Group.fetchInstance(context.getConnection().getObjectStore().getConnection(), ap.get_GranteeName(), null);
//                    System.out.println(group.get_DistinguishedName() + " " + group.get_Users());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            boolean first = true;
//            if ((ap.get_AccessMask() & AccessRight.ADD_MARKING_AS_INT) == AccessRight.ADD_MARKING_AS_INT) {
//                first = appendValue(sb, first, "AddMarking");
//            }
//            if ((ap.get_AccessMask() & AccessRight.CHANGE_STATE_AS_INT) == AccessRight.CHANGE_STATE_AS_INT) {
//                first = appendValue(sb, first, "ChangeState");
//            }
//            if ((ap.get_AccessMask() & AccessRight.CONNECT_AS_INT) == AccessRight.CONNECT_AS_INT) {
//                first = appendValue(sb, first, "Connect");
//            }
//            if ((ap.get_AccessMask() & AccessRight.CREATE_CHILD_AS_INT) == AccessRight.CREATE_CHILD_AS_INT) {
//                first = appendValue(sb, first, "CreateChild");
//            }
//            if ((ap.get_AccessMask() & AccessRight.CREATE_INSTANCE_AS_INT) == AccessRight.CREATE_INSTANCE_AS_INT) {
//                first = appendValue(sb, first, "CreateInstance");
//            }
//            if ((ap.get_AccessMask() & AccessRight.DELETE_AS_INT) == AccessRight.DELETE_AS_INT) {
//                first = appendValue(sb, first, "Delete");
//            }
//            if ((ap.get_AccessMask() & AccessRight.LINK_AS_INT) == AccessRight.LINK_AS_INT) {
//                first = appendValue(sb, first, "Link");
//            }
//            if ((ap.get_AccessMask() & AccessRight.MAJOR_VERSION_AS_INT) == AccessRight.MAJOR_VERSION_AS_INT) {
//                first = appendValue(sb, first, "MajorVersion");
//            }
//            if ((ap.get_AccessMask() & AccessRight.MINOR_VERSION_AS_INT) == AccessRight.MINOR_VERSION_AS_INT) {
//                first = appendValue(sb, first, "MinorVersion");
//            }
//            if ((ap.get_AccessMask() & AccessRight.MODIFY_OBJECTS_AS_INT) == AccessRight.MODIFY_OBJECTS_AS_INT) {
//                first = appendValue(sb, first, "ModifyObjects");
//            }
//            if ((ap.get_AccessMask() & AccessRight.MODIFY_RETENTION_AS_INT) == AccessRight.MODIFY_RETENTION_AS_INT) {
//                first = appendValue(sb, first, "ModifyRetention");
//            }
//            if ((ap.get_AccessMask() & AccessRight.NONE_AS_INT) == AccessRight.NONE_AS_INT) {
//                first = appendValue(sb, first, "None");
//            }
//            if ((ap.get_AccessMask() & AccessRight.PRIVILEGED_WRITE_AS_INT) == AccessRight.PRIVILEGED_WRITE_AS_INT) {
//                first = appendValue(sb, first, "PrivelegedWrite");
//            }
//            if ((ap.get_AccessMask() & AccessRight.PUBLISH_AS_INT) == AccessRight.PUBLISH_AS_INT) {
//                first = appendValue(sb, first, "Publish");
//            }
//            if ((ap.get_AccessMask() & AccessRight.READ_ACL_AS_INT) == AccessRight.READ_ACL_AS_INT) {
//                first = appendValue(sb, first, "ReadACL");
//            }
//            if ((ap.get_AccessMask() & AccessRight.READ_AS_INT) == AccessRight.READ_AS_INT) {
//                first = appendValue(sb, first, "Read");
//            }
//            if ((ap.get_AccessMask() & AccessRight.REMOVE_MARKING_AS_INT) == AccessRight.REMOVE_MARKING_AS_INT) {
//                first = appendValue(sb, first, "RemoveMarking");
//            }
//            if ((ap.get_AccessMask() & AccessRight.REMOVE_OBJECTS_AS_INT) == AccessRight.REMOVE_OBJECTS_AS_INT) {
//                first = appendValue(sb, first, "RemoveObjects");
//            }
//            if ((ap.get_AccessMask() & AccessRight.RESERVED12_AS_INT) == AccessRight.RESERVED12_AS_INT) {
//                first = appendValue(sb, first, "Reserved12");
//            }
//            if ((ap.get_AccessMask() & AccessRight.RESERVED13_AS_INT) == AccessRight.RESERVED13_AS_INT) {
//                first = appendValue(sb, first, "Reserved13");
//            }
//            if ((ap.get_AccessMask() & AccessRight.STORE_OBJECTS_AS_INT) == AccessRight.STORE_OBJECTS_AS_INT) {
//                first = appendValue(sb, first, "StoreObjects");
//            }
//            if ((ap.get_AccessMask() & AccessRight.UNLINK_AS_INT) == AccessRight.UNLINK_AS_INT) {
//                first = appendValue(sb, first, "Unlink");
//            }
//            if ((ap.get_AccessMask() & AccessRight.USE_MARKING_AS_INT) == AccessRight.USE_MARKING_AS_INT) {
//                first = appendValue(sb, first, "UseMarking");
//            }
//            if ((ap.get_AccessMask() & AccessRight.VIEW_CONTENT_AS_INT) == AccessRight.VIEW_CONTENT_AS_INT) {
//                first = appendValue(sb, first, "ViewContent");
//            }
//            if ((ap.get_AccessMask() & AccessRight.VIEW_RECOVERABLE_OBJECTS_AS_INT) == AccessRight.VIEW_RECOVERABLE_OBJECTS_AS_INT) {
//                first = appendValue(sb, first, "ViewRecoverableObjects");
//            }
//            if ((ap.get_AccessMask() & AccessRight.WRITE_ACL_AS_INT) == AccessRight.WRITE_ACL_AS_INT) {
//                first = appendValue(sb, first, "WriteACL");
//            }
//            if ((ap.get_AccessMask() & AccessRight.WRITE_ANY_OWNER_AS_INT) == AccessRight.WRITE_ANY_OWNER_AS_INT) {
//                first = appendValue(sb, first, "WriteAnyOwner");
//            }
//            if ((ap.get_AccessMask() & AccessRight.WRITE_AS_INT) == AccessRight.WRITE_AS_INT) {
//                first = appendValue(sb, first, "Write");
//            }
//            if ((ap.get_AccessMask() & AccessRight.WRITE_OWNER_AS_INT) == AccessRight.WRITE_OWNER_AS_INT) {
//                first = appendValue(sb, first, "WriteOwner");
//            }
//
//            sb.append("\" />");

        }
    }
}
