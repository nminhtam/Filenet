/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator;

import java.util.Comparator;

import com.filenet.api.admin.PropertyDefinition;

/**
 * @author NguyenT
 *
 */
public class PropertyDefinitionComparator implements Comparator<PropertyDefinition> {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(PropertyDefinition arg0, PropertyDefinition arg1) {
        if (arg0 != null && arg1 == null) {
            return 1;
        }
        if (arg1 != null && arg0 == null) {
            return -1;
        }
        return arg0.get_SymbolicName().compareTo(arg1.get_SymbolicName());
    }

}