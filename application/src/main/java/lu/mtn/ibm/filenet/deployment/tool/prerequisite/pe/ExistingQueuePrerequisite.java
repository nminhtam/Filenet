/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe;

import filenet.vw.api.VWQueueDefinition;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingQueuePrerequisite implements Prerequisite {

    private String componentQueue;

    private boolean mustExist;

    /**
     * @param componentQueue
     */
    public ExistingQueuePrerequisite(String componentQueue, boolean mustExist) {
        super();
        this.componentQueue = componentQueue;
        this.mustExist = mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {

        VWQueueDefinition queue = context.getConnection().getVWSession().fetchSystemConfiguration().getQueueDefinition(componentQueue);
        return (queue != null) == mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#isBlocking()
     */
    @Override
    public boolean isBlocking() {
        return true;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#getDescription()
     */
    @Override
    public String getDescription() {
        return "The Queue " + this.componentQueue + " must exist.";
    }

}
