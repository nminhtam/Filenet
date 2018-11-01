/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;

/**
 * @author NguyenT
 *
 */
public interface Prerequisite {


    /**
     * @param context
     * @return true if the prerequisite is met thus the execution of the operation can continue
     */
    public boolean check(ExecutionContext context);

    public boolean isBlocking();

    public String getDescription();
}
