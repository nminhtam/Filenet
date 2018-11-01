/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce;

import java.util.Collection;

import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.dto.Permission;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.UpdateClassSecurity;

/**
 * @author NguyenT
 *
 */
public class ClassSecurityMapper extends AbstractSecurityMapper {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public ClassSecurityMapper(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#isApplicable(be.portima.dmpoc.installation.operation.Operation)
     */
    @Override
    public boolean isApplicable(Operation operation) {
        if (operation instanceof UpdateClassSecurity) {
            return true;
        }
        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#modify(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public void modify(Operation operation) throws OperationExecutionException {

        UpdateClassSecurity op = (UpdateClassSecurity) operation;

        Collection<Permission> permissions = op.getpermissions();
        if (permissions != null) {
            for (Permission permission : permissions) {

                String substitute = this.mappings.get(permission.getUser());
                if (substitute != null) {
                    permission.setUser(substitute);
                }
            }
        }
        permissions = op.getDefaultInstancePermissions();
        if (permissions != null) {
            for (Permission permission : permissions) {

                String substitute = this.mappings.get(permission.getUser());
                if (substitute != null) {
                    permission.setUser(substitute);
                }
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Register a class security mapper.";
    }

}
