/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator;

import java.util.Comparator;

import com.filenet.api.admin.ClassDefinition;

/**
 * @author NguyenT
 *
 */
public class ClassDefinitionComparator implements Comparator<ClassDefinition> {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(ClassDefinition arg0, ClassDefinition arg1) {
        if (arg0 != null && arg1 == null) {
            return 1;
        }
        if (arg1 != null && arg0 == null) {
            return -1;
        }
        return arg0.get_SymbolicName().compareTo(arg1.get_SymbolicName());
    }

}