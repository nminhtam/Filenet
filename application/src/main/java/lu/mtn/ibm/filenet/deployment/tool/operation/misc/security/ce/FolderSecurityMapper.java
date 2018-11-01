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
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.UpdateFolderSecurity;

/**
 * @author NguyenT
 *
 */
public class FolderSecurityMapper extends AbstractSecurityMapper {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public FolderSecurityMapper(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#isApplicable(be.portima.dmpoc.installation.operation.Operation)
     */
    @Override
    public boolean isApplicable(Operation operation) {
        if (operation instanceof UpdateFolderSecurity) {
            return true;
        }
        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#modify(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public void modify(Operation operation) throws OperationExecutionException {

        Collection<Permission> permissions = ((UpdateFolderSecurity) operation).getpermissions();
        for (Permission permission : permissions) {

            String substitute = this.mappings.get(permission.getUser());
            if (substitute != null) {
                permission.setUser(substitute);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Register a folder security mapper.";
    }
}
