/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator;

import java.util.Comparator;

import com.filenet.api.security.AccessPermission;

/**
 * @author NguyenT
 *
 */
public class AccessPermissionComparator implements Comparator<AccessPermission> {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(AccessPermission arg0, AccessPermission arg1) {
        if (arg0 != null && arg1 == null) {
            return 1;
        }
        if (arg1 != null && arg0 == null) {
            return -1;
        }
        return arg0.get_GranteeName().compareTo(arg1.get_GranteeName());
    }

}
